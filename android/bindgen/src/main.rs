//! `uniffi-bindgen` for `prostudio-core`: generates its bindings from the built library, e.g.
//! `cargo run -p prostudio-bindgen -- generate libprostudio_core.so --language kotlin --out-dir out`
//! (`android/app/build.gradle.kts` runs it; `ios/build-rust.sh` runs it for Swift).

fn main() {
    uniffi::uniffi_bindgen_main()
}
