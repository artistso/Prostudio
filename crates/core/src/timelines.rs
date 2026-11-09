//! Multiple timelines in one artwork (scenes, as a traditional animation suite calls them):
//! listing, adding, renaming, deleting and switching them. Animation Assist's bar shows
//! them as lanes above the frames; each timeline carries its own settings and its own frames.

use prostudio_engine::{document::Document, timeline::Timeline};

use crate::CanvasSession;

/// One timeline, as the timeline bar shows them.
#[derive(Clone, Debug, PartialEq, uniffi::Record)]
pub struct TimelineInfo {
    /// Stable identity, unique in the artwork while it lives.
    pub id: u64,
    /// Shown in the lane.
    pub name: String,
    /// The frames it has (its visible layers, one per frame).
    pub frames: u32,
    /// In play when true.
    pub active: bool,
}

fn timeline_info(doc: &Document, t: &Timeline) -> TimelineInfo {
    TimelineInfo {
        id: t.id,
        name: t.name.clone(),
        frames: u32::try_from(doc.timeline_frames(t.id).len()).unwrap_or(u32::MAX),
        active: doc.timelines().active == Some(t.id),
    }
}

#[uniffi::export]
impl CanvasSession {
    /// The artwork's timelines, in the order they show. Empty while Animation Assist or
    /// Page Assist has never been on.
    pub fn timelines(&self) -> Vec<TimelineInfo> {
        let doc = &self.shared.lock().doc;
        doc.timelines().items.iter().map(|t| timeline_info(doc, t)).collect()
    }

    /// A fresh timeline ("Timeline N", or `name`), made active. The artwork's first one is
    /// made from what it shows: its settings, if any, and its visible top-level layers or
    /// groups as its frames. Returns the new timeline's id, or 0 on failure.
    pub fn add_timeline(&self, name: Option<String>) -> u64 {
        let id = self.change("add timeline", |d| Some(d.add_timeline(name))).unwrap_or(0);
        self.timeline_switched();
        id
    }

    /// Makes timeline `id` the active one. False for no such timeline.
    pub fn switch_timeline(&self, id: u64) -> bool {
        let switched = self.change("switch timeline", |d| d.set_active_timeline(id).then_some(())).is_some();
        if switched {
            self.timeline_switched();
        }
        switched
    }

    /// Renames timeline `id`; false for no such timeline or an empty name.
    pub fn rename_timeline(&self, id: u64, name: String) -> bool {
        self.change("rename timeline", |d| d.rename_timeline(id, name).then_some(())).is_some()
    }

    /// Deletes timeline `id` — never the last one. The artwork stays with the one after
    /// it (or before it). False for no such timeline, or for the last one.
    pub fn delete_timeline(&self, id: u64) -> bool {
        let deleted = self.change("delete timeline", |d| d.delete_timeline(id).then_some(())).is_some();
        if deleted {
            self.timeline_switched();
        }
        deleted
    }
}

impl CanvasSession {
    /// Playing starts over, the canvas redraws: the switch changed whose frames are in play.
    fn timeline_switched(&self) {
        let mut guard = self.shared.lock();
        let st = &mut *guard;
        if let Some(c) = st.canvas.as_mut() {
            c.set_play_frame(None);
            c.invalidate();
        }
        self.shared.frame(st);
    }
}
