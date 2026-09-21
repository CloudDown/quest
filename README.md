# Quest

Android app + FastAPI API — one place per day, check in on site.

**APK**: [GitHub Releases](https://github.com/CloudDown/quest/releases/latest)  
**Public API**: https://quest.instree.org

## Usage

```bash
./server.sh
./release-github.sh   # GitHub + in-app update popup
./run-cable.sh        # USB phone (adb)
./run-cable.sh --build
```

Keep `./server.sh` running after a release: the app offers to install on its own.

LAN: `./server.sh --local` then `./release-github.sh lan`.

Demo accounts: `rain` / `rain`, `alex` / `alex`.

Cursor conventions: [AGENTS.md](AGENTS.md)
