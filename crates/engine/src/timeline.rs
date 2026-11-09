//! Timelines: an artwork may hold more than one Animation Assist timeline (a scene, as a
//! traditional animation suite calls it), each with its own settings and its own frames.
//!
//! A file saved before timelines existed carries at most one `animation` object; on
//! [`crate::document::Document::repaired`] it is moved into the artwork's first timeline, so
//! the artwork opens with the frames it always had, numbered "Timeline 1". The `animation`
//! field is never written back once that has happened.
//!
//! A timeline's frames are layers (one layer per frame, as Animation Assist has always drawn
//! them): a frame references a top-level layer or group. Frames whose layer is gone or hidden
//! are skipped, so a stale reference can never draw anything. Membership is explicit in
//! timeline mode: layers made afterwards join a timeline through `Add Frame`, not on their own.

use crate::animation::Animation;
use crate::document::{Document, LayerId};

/// One timeline (a scene): its name, its own Animation Assist settings, and its frames.
#[derive(Clone, Debug, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct Timeline {
    /// Stable identity within the artwork, handed out in order from 1.
    pub id: u64,
    /// Shown in the timeline bar; "Timeline 1" as added.
    pub name: String,
    /// Its Animation Assist settings, in force while it is the active timeline.
    #[serde(default = "Animation::default")]
    pub settings: Animation,
    /// The frames, one layer each, left to right.
    #[serde(default)]
    pub frames: Vec<LayerId>,
}

/// The artwork's timelines, in the order they show.
#[derive(Clone, Debug, PartialEq, serde::Serialize, serde::Deserialize)]
pub struct Timelines {
    /// The timelines; empty while the artwork has none (a fresh artwork, or one saved
    /// before timelines existed with Animation Assist off).
    #[serde(default)]
    pub items: Vec<Timeline>,
    /// Which one is in play: its id. None while there are no timelines.
    #[serde(default)]
    pub active: Option<u64>,
    /// Whether the timeline is shown and in force (Animation Assist on). Turning it off
    /// hides the bar, onion skins and playing; the timelines themselves stay.
    #[serde(default = "on")]
    pub enabled: bool,
}

const fn on() -> bool {
    true
}

impl Default for Timelines {
    fn default() -> Timelines {
        Timelines { items: Vec::new(), active: None, enabled: true }
    }
}

impl Timelines {
    /// Whether the artwork is in the legacy configuration, holding no timeline yet.
    pub fn is_legacy(&self) -> bool {
        self.items.is_empty()
    }

    /// The active timeline.
    pub fn active(&self) -> Option<&Timeline> {
        let id = self.active?;
        self.items.iter().find(|t| t.id == id)
    }

    fn active_mut(&mut self) -> Option<&mut Timeline> {
        let id = self.active?;
        self.items.iter_mut().find(|t| t.id == id)
    }

    /// The id of the next timeline to hand out.
    fn next_id(&self) -> u64 {
        self.items.iter().map(|t| t.id).max().unwrap_or(0).saturating_add(1)
    }
}

impl Document {
    /// The artwork's timelines (empty while it is in the legacy configuration).
    pub fn timelines(&self) -> &Timelines {
        &self.timelines
    }

    /// The settings in force — the active timeline's; none while the timeline is hidden
    /// or the artwork has no timeline.
    pub fn active_animation(&self) -> Option<&Animation> {
        if !self.timelines.enabled {
            return None;
        }
        self.timelines.active().map(|t| &t.settings)
    }

    /// The settings in force, to change in place.
    pub fn active_animation_mut(&mut self) -> Option<&mut Animation> {
        if !self.timelines.enabled {
            return None;
        }
        self.timelines.active_mut().map(|t| &mut t.settings)
    }

    /// Replaces the settings in force (none hides the bar, keeping the timelines); a
    /// fresh artwork, or one with none, gets them in a new first timeline seeded from
    /// what it shows.
    pub fn set_animation(&mut self, a: Option<Animation>) {
        match a {
            Some(a) => {
                if self.timelines.items.is_empty() {
                    self.seed_timeline(Some(a), None);
                } else {
                    self.timelines.enabled = true;
                    if let Some(t) = self.timelines.active_mut() {
                        t.settings = a;
                    }
                }
            }
            None => self.timelines.enabled = false,
        }
    }

    /// Turns the timeline on in `pages` (Page Assist) or Animation Assist mode, keeping
    /// every other setting. While there is no timeline it is seeded from what the artwork
    /// shows, with fresh settings.
    pub fn set_assist(&mut self, pages: bool) {
        if self.timelines.items.is_empty() {
            self.seed_timeline(Some(Animation { pages, ..Animation::default() }), None);
            return;
        }
        self.timelines.enabled = true;
        if let Some(t) = self.timelines.active_mut() {
            t.settings.pages = pages;
        }
    }

    /// The frames of the active timeline, one layer each, left to right. The legacy
    /// configuration's frames are every visible top-level layer or group, as they always
    /// were. Frames whose layer is gone or hidden are skipped.
    pub fn active_frames(&self) -> Vec<LayerId> {
        if self.timelines.is_legacy() {
            return crate::animation::frames(self);
        }
        let Some(active) = self.timelines.active() else { return Vec::new() };
        self.timeline_frames(active.id)
    }

    /// The frames of timeline `id`, one layer each, skipping any whose layer is gone or
    /// hidden — as [`Self::active_frames`] does for the active one. Empty for no such
    /// timeline.
    pub fn timeline_frames(&self, id: u64) -> Vec<LayerId> {
        let Some(t) = self.timelines.items.iter().find(|t| t.id == id) else { return Vec::new() };
        t.frames.iter().copied().filter(|id| self.layer(*id).is_some_and(|l| l.visible)).collect()
    }

    /// Inserts `id` into the active timeline's frames right after `after` (at the end
    /// while `after` isn't one of them); no-op in the legacy configuration. Used when a
    /// frame is split, so the copy takes its place right after the original. Returns
    /// whether it was inserted.
    pub fn insert_frame_after(&mut self, after: LayerId, id: LayerId) -> bool {
        if self.timelines.is_legacy() {
            return false;
        }
        let Some(active) = self.timelines.active_mut() else { return false };
        if active.frames.contains(&id) {
            return false;
        }
        match active.frames.iter().position(|f| *f == after) {
            Some(at) => active.frames.insert(at + 1, id),
            None => active.frames.push(id),
        }
        true
    }

    /// Adds `id` to the active timeline's frames (in timeline mode; nothing in the legacy
    /// configuration, where every visible top-level unit is a frame). Returns whether it
    /// was added.
    pub fn adopt_frame(&mut self, id: LayerId) -> bool {
        if self.timelines.is_legacy() {
            return false;
        }
        let Some(active) = self.timelines.active_mut() else { return false };
        if active.frames.contains(&id) {
            return false;
        }
        active.frames.push(id);
        true
    }

    /// A fresh timeline with `name` (its default is "Timeline N"), made the active one;
    /// returns its id. While the artwork has none (Animation Assist never turned on, or a
    /// fresh canvas), the first timeline is seeded from what it shows — its legacy settings,
    /// if any, and every visible top-level unit as its frames.
    pub fn add_timeline(&mut self, name: Option<String>) -> u64 {
        if self.timelines.items.is_empty() {
            let seed = self.animation.take();
            return self.seed_timeline(seed, name);
        }
        let id = self.timelines.next_id();
        let name = name.and_then(|n| clean_name(&n)).unwrap_or_else(|| format!("Timeline {id}"));
        self.timelines.items.push(Timeline { id, name, settings: Animation::default(), frames: Vec::new() });
        self.timelines.active = Some(id);
        self.timelines.enabled = true;
        id
    }

    /// The first timeline, seeded from the artwork's legacy settings (or defaults) and its
    /// visible top-level units as its frames.
    fn seed_timeline(&mut self, settings: Option<Animation>, name: Option<String>) -> u64 {
        let id = self.timelines.next_id();
        let name = name.and_then(|n| clean_name(&n)).unwrap_or_else(|| format!("Timeline {id}"));
        let frames = crate::animation::frames(self);
        self.timelines.items.push(Timeline { id, name, settings: settings.unwrap_or_default(), frames });
        self.timelines.active = Some(id);
        self.timelines.enabled = true;
        id
    }

    /// Makes `id` the active timeline (and shows it, if it was hidden); false for no such
    /// timeline.
    pub fn set_active_timeline(&mut self, id: u64) -> bool {
        if !self.timelines.items.iter().any(|t| t.id == id) {
            return false;
        }
        self.timelines.active = Some(id);
        self.timelines.enabled = true;
        true
    }

    /// Renames a timeline; false for no such timeline, nothing changed for an empty name.
    pub fn rename_timeline(&mut self, id: u64, name: String) -> bool {
        let Some(name) = clean_name(&name) else { return false };
        let Some(t) = self.timelines.items.iter_mut().find(|t| t.id == id) else { return false };
        t.name = name;
        true
    }

    /// Deletes a timeline. The last one is kept — an artwork never ends up timeline-less
    /// once it has timelines. The timeline after the deleted one (or the one before it)
    /// becomes active. Returns whether one was deleted.
    pub fn delete_timeline(&mut self, id: u64) -> bool {
        if self.timelines.items.len() <= 1 {
            return false;
        }
        let Some(index) = self.timelines.items.iter().position(|t| t.id == id) else { return false };
        let was_active = self.timelines.active == Some(id);
        self.timelines.items.remove(index);
        if was_active {
            let next = index.min(self.timelines.items.len() - 1);
            self.timelines.active = self.timelines.items.get(next).map(|t| t.id);
        }
        true
    }

    /// A document read from a file, made consistent: timeline settings in range, names
    /// trimmed, frames pointing at layers that exist, the active timeline valid — and the
    /// legacy `animation` object, if there is one, moved into the artwork's first timeline.
    pub(crate) fn normalize_timelines(&mut self) {
        if self.timelines.items.is_empty() {
            if let Some(a) = self.animation.take() {
                let frames = crate::animation::frames(self);
                self.timelines.items.push(Timeline { id: 1, name: "Timeline 1".to_owned(), settings: a, frames });
                self.timelines.active = Some(1);
            }
        }
        let ids: Vec<LayerId> = self.layers().iter().map(|l| l.id).collect();
        for t in &mut self.timelines.items {
            let settings = std::mem::take(&mut t.settings);
            t.settings = settings.repaired();
            let name = t.name.trim();
            t.name = if name.is_empty() { "Timeline".to_owned() } else { name.chars().take(64).collect() };
            t.frames.retain(|id| ids.contains(id));
        }
        let active_valid = self.timelines.active.is_some_and(|id| self.timelines.items.iter().any(|t| t.id == id));
        if !active_valid && !self.timelines.items.is_empty() {
            self.timelines.active = self.timelines.items.first().map(|t| t.id);
        }
        self.animation = None;
    }
}

/// A trimmed, nonempty name (capped), or none.
fn clean_name(name: &str) -> Option<String> {
    let name = name.trim().chars().take(64).collect::<String>();
    (!name.is_empty()).then_some(name)
}

#[cfg(test)]
mod tests {
    use super::*;

    fn doc_with_paint_layers(count: usize) -> Document {
        let mut doc = Document::new(64, 64);
        for _ in 1..count {
            doc.add_layer();
        }
        doc
    }

    #[test]
    fn a_legacy_artwork_with_assist_on_opens_in_timeline_one() {
        let mut doc = doc_with_paint_layers(3);
        doc.animation = Some(Animation { fps: 24, ..Animation::default() });
        let doc = doc.repaired();
        let timelines = doc.timelines();
        assert_eq!(timelines.items.len(), 1);
        assert_eq!(timelines.items[0].name, "Timeline 1");
        assert_eq!(timelines.items[0].settings.fps, 24);
        assert_eq!(timelines.items[0].frames, doc.members(None));
        assert_eq!(doc.active_animation().map(|a| a.fps), Some(24));
        assert_eq!(doc.active_frames().len(), 3);
        assert!(doc.animation.is_none(), "the legacy object is consumed, not duplicated");
    }

    #[test]
    fn a_legacy_artwork_with_assist_off_stays_legacy() {
        let doc = doc_with_paint_layers(2).repaired();
        assert!(doc.timelines().is_legacy());
        assert!(doc.active_animation().is_none());
        assert_eq!(doc.active_frames().len(), 2);
    }

    #[test]
    fn turning_assist_on_seeds_timeline_one_and_back_off_keeps_it() {
        let mut doc = doc_with_paint_layers(2).repaired();
        doc.set_animation(Some(Animation { fps: 30, ..Animation::default() }));
        assert_eq!(doc.timelines().items.len(), 1);
        assert_eq!(doc.active_animation().map(|a| a.fps), Some(30));
        assert_eq!(doc.active_frames().len(), 2);
        doc.set_animation(None);
        assert!(doc.active_animation().is_none());
        assert_eq!(doc.timelines().items.len(), 1, "the timeline stays");
        doc.set_animation(Some(Animation { fps: 12, ..Animation::default() }));
        assert_eq!(doc.active_animation().map(|a| a.fps), Some(12));
    }

    #[test]
    fn a_second_timeline_starts_empty_and_switching_keeps_settings_and_frames_apart() {
        let mut doc = doc_with_paint_layers(2).repaired();
        doc.set_animation(Some(Animation { fps: 24, ..Animation::default() }));
        let first = doc.timelines().active.expect("a timeline is active");
        doc.set_animation(Some(Animation { fps: 24, holds: vec![(LayerId(1), 3)], ..Animation::default() }));
        let second = doc.add_timeline(None);
        assert_ne!(first, second);
        assert_eq!(doc.timelines().items[1].name, "Timeline 2");
        assert!(doc.active_frames().is_empty());
        assert_eq!(doc.active_animation().map(|a| a.fps), Some(15));
        let adopted = doc.adopt_frame(LayerId(1));
        assert!(adopted);
        assert!(!doc.adopt_frame(LayerId(1)), "no duplicates");
        assert!(doc.set_active_timeline(first));
        assert_eq!(doc.active_frames().len(), 2);
        assert_eq!(doc.active_animation().and_then(|a| a.holds.first()).map(|h| h.1), Some(3));
        assert!(doc.set_active_timeline(second));
        assert_eq!(doc.active_frames(), vec![LayerId(1)]);
        assert!(doc.active_animation().is_some_and(|a| a.holds.is_empty()));
    }

    #[test]
    fn the_last_timeline_cannot_be_deleted_and_deleting_the_active_activates_a_neighbour() {
        let mut doc = doc_with_paint_layers(1).repaired();
        doc.set_animation(Some(Animation::default()));
        let first = doc.timelines().active.expect("active");
        assert!(!doc.delete_timeline(first), "the last timeline stays");
        let second = doc.add_timeline(Some("Intro".to_owned()));
        assert_eq!(doc.timelines().items.len(), 2);
        assert!(doc.delete_timeline(second));
        assert_eq!(doc.timelines().active, Some(first));
        // deleting a non-active timeline keeps the active one
        let second = doc.add_timeline(None);
        assert!(doc.set_active_timeline(first));
        assert!(doc.delete_timeline(second));
        assert_eq!(doc.timelines().active, Some(first));
    }

    #[test]
    fn renaming_trims_and_refuses_empty() {
        let mut doc = doc_with_paint_layers(1).repaired();
        doc.set_animation(Some(Animation::default()));
        let id = doc.timelines().active.expect("active");
        assert!(doc.rename_timeline(id, "  Main  ".to_owned()));
        assert_eq!(doc.timelines().items[0].name, "Main");
        assert!(!doc.rename_timeline(id, "   ".to_owned()));
    }

    #[test]
    fn timelines_survive_a_save_and_load_roundtrip_and_a_legacy_file_still_reads() {
        let mut doc = doc_with_paint_layers(2).repaired();
        doc.set_animation(Some(Animation { fps: 24, ..Animation::default() }));
        let second = doc.add_timeline(Some("Extra".to_owned()));
        doc.adopt_frame(LayerId(1));
        let text = serde_json::to_string(&doc).expect("serializes");
        assert!(!text.contains("\"animation\""), "timelines carry the settings; the legacy object is gone");
        let back: Document = serde_json::from_str(&text).expect("reads").repaired();
        assert_eq!(back.timelines().items.len(), 2);
        assert_eq!(back.timelines().active, Some(second));
        assert_eq!(back.timelines().items[1].name, "Extra");
        // a legacy file: serialized before repair, with one animation object and no timelines
        let mut legacy = doc_with_paint_layers(2);
        legacy.animation = Some(Animation { fps: 18, ..Animation::default() });
        let text = serde_json::to_string(&legacy).expect("serializes");
        assert!(text.contains("\"animation\""));
        let from_legacy: Document = serde_json::from_str(&text).expect("legacy reads").repaired();
        assert_eq!(from_legacy.timelines().items.len(), 1);
        assert_eq!(from_legacy.active_animation().map(|a| a.fps), Some(18));
    }

    #[test]
    fn frames_pointing_at_missing_layers_are_pruned_on_open() {
        let mut doc = doc_with_paint_layers(2).repaired();
        doc.set_animation(Some(Animation::default()));
        doc.adopt_frame(LayerId(1));
        doc.adopt_frame(LayerId(9999));
        let text = serde_json::to_string(&doc).expect("serializes");
        let back: Document = serde_json::from_str(&text).expect("reads").repaired();
        assert_eq!(back.active_frames(), vec![LayerId(1), LayerId(2)]);
        let adopted: Vec<LayerId> = back.timelines().items[0].frames.clone();
        assert!(adopted.contains(&LayerId(1)));
        assert!(!adopted.contains(&LayerId(9999)));
    }
}
