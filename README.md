<center><h1>Highlight and Extractor<p>Burp Extension</h1></center>

## About

This an altered version of the great **HaE Burp Extension** based on version 2.4.6.

Thanks to <a href="https://github.com/gh0stkey">EvilChen</a> and <a href="https://github.com/0chencc">0chencc</a> for the original version https://github.com/gh0stkey/HaE

The Portswigger version can be found on https://github.com/portswigger/highlighter-and-extractor

## What's Different

This is a great Burp extensions, but there were a few things I wanted to change, so I forked the original. Here's what's different:

- Add `none` option for highlight
- Default a new rule to `none`
- Show comments split with `|` instead of `,` to make it easier to read
- Only search and highlight `In Scope` requests and responses
- Add more file extension exclusions
- Add a config option for **Hghlight Method**. The original does colour upscaling, which means that if a number of matches are found for one request/response, the colour can end up being upscaled and shown as colour of increased severity. I personally preferred to have a highlight of the most severe finding only (this allows a request/response not to be highlighted at all, even if there ar 5 `none` rules fired).

## Excuses

I'm not a Java programmer and I don't understand creating a proper UI, so the new Highlight Method option on the config tab is not aligned properly... but who cares?!

## Notes

- If you want to use regex with special characters like `\n`, `\s`, etc. then you need to use engine `nfa`
- Case sensitive only works for engine `nfa`, therefore if you use `dfa` you need to make sure you deal with different cases.
- Apparently `dfa` is faster than `nfa`
- You can search `Databoard` with `*.` (I;m not sure this change was in the Portswigger version).
- You need to wrap the regex of rules in `()` to work correctly.
- Apparently `dfa` is faster than `nfa`
- Disable any rules that you don't need or want as they can slow thing down.

Feel free to use this version too!

Good luck and good hunting!
If you really love the tool (or any others), or they helped you find an awesome bounty, consider [BUYING ME A COFFEE!](https://ko-fi.com/xnlh4ck3r) ☕ (I could use the caffeine!)

🤘 /XNL-h4ck3r
