<div align="center">
  <h1>Gooseboy</h1>
  <a href="https://fabricmc.net/"><img alt="Available for Fabric" src="https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/supported/fabric_vector.svg" /></a>
  <a href="https://modrinth.com/mod/fabric-api"><img alt="Requires Fabric API" src="https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/requires/fabric-api_vector.svg" /></a>
  <a href="https://modrinth.com/mod/gooseboy/gallery"><img alt="Check out the gallery" src="https://raw.githubusercontent.com/intergrav/devins-badges/c7fd18efdadd1c3f12ae56b49afd834640d2d797/assets/cozy/documentation/modrinth-gallery_vector.svg" /></a>
  <h3>A tiny game console inside Minecraft</h3>
</div>

This mod allows you to run [WebAssembly](https://webassembly.org/) scripts inside Minecraft on the client safely, allowing the creation of games, programs, and whatever your heart desires.

> Disclaimer: Scripts are called crates in Gooseboy

## Features
- Run games and applications inside Minecraft with minimal performance impact
- Make games and applications using [Rust](https://rust-lang.org/) and [the standard library](https://github.com/awildergoose/gooseboy-rs)
- Per-crate permission control and storage
- Crate support for playing any audio, without reloading the game

## Permissions
Every crate by default has very minimal permissions to only the console and the mouse, you can change the permissions for each crate by opening the settings of the crate.
The full list of permissions are as follows:
- **CONSOLE**: Allows the crate to log messages to the console.
- **AUDIO**: Allows the crate to play sounds and/or music, with any audio.
- **INPUT_KEYBOARD**: Allows the crate to know if you pressed a key.
- **INPUT_MOUSE**: Allows the crate to know if you clicked with your mouse.
- **INPUT_MOUSE_POS**: Allows the crate to know the position of your mouse, adjusted to the region of the screen.
- **STORAGE_READ**: Allows the crate to read from that crate's 512 KBs of storage.
- **STORAGE_WRITE**: Allows the crate to write to its 512 KBs of storage.
- **EXTENDED_MEMORY**: Allocates 64 MB of memory to the crate instead of the regular 8 MB

### Security
Crates are run using the Java [Chicory](https://chicory.dev/) WASM runtime.
For security, crates can't:
- access or modify Minecraft's state (e.g. where other players are in-game)
- access or modify other crates' storage
- access or modify your files
- render onto the world
- run in the background
- read your keyboard or mouse inputs outside of the game
- use your internet or communicate with any server
- run java code

If you’ve found a crate that can bypass these restrictions, please report it via [GitHub](https://github.com/awildergoose/).

## How-to add crates
You can add crates by moving them to the crates folder. You can open the crates folder from the menu by pressing the "Open crates folder location" button at the bottom.

## How-to make crates
You can make crates in any language that compiles to WebAssembly (WASI is unsupported due to security concerns).
The recommended language to use is [Rust](https://rust-lang.org/), and so the [standard library](https://github.com/awildergoose/gooseboy-rs) is made in Rust as well.

An example crate is as follows:
```rs
#![no_main]

use gooseboy::framebuffer::{get_framebuffer_width, init_fb};
use gooseboy::text::{draw_text, get_text_width};
use gooseboy::{color::Color, framebuffer::clear_framebuffer};

// Every crate has to have a main function, make sure to decorate it
// with gooseboy::main though, or else the crate won't start
#[gooseboy::main]
fn main() {
    // Initializes the framebuffer, you are required to initialize this
    // here if you plan to draw to the screen (which is very likely)
    init_fb();
}

// This is also required in every crate, the gooseboy::update is required
// here too, this function runs X times per second where X is equal to your
// maximum framerate in the options
#[gooseboy::update]
fn update(nano_time: i64) {
    // Clear out the screen, erasing everything that was there previously
    clear_framebuffer(Color::BLACK);

    // Initialize the string we want to draw to the screen, You can also use Rust's
    // String type here, with the caveat of having to clone it at draw_text
    let text = "Hello, world!";
    // Convert the time from nanoseconds to seconds
    let time_sec = nano_time as f64 / 1_000_000_000.0;
    // Get the position of the right corner and subtract the width of the text
    // to make the text fit into the screen, You can also use draw_text_wrapped
    // to automatically wrap text if it passes the end of the framebuffer
    let right_corner = (get_framebuffer_width() - get_text_width(text)) as f64;
    // Gets us an X position that smoothly moves from the left to the right using sine
    let x_pos = ((time_sec.sin() * 0.5 + 0.5) * (right_corner - 1.0)) as usize;

    // Finally, draw the text with the red color (or use Color::new(r, g, b, a) or Color::new_opaque(r, g, b))
    draw_text(x_pos, 0, text, Color::RED);
}
```

## Contributing
Want to help improve Gooseboy? Contributions are welcome! You can find the standard library [here](https://github.com/awildergoose/gooseboy-rs) and the Java mod [here](https://github.com/awildergoose/gooseboy)!
