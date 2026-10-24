"""Port-only: compares the port's lang files with upstream's and writes test-output/playtest/lang.md.

Usage: py -3 tools/compare_lang.py [version folder, default 1.20.1]
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VERSION = sys.argv[1] if len(sys.argv) > 1 else "1.20.1"
UPSTREAM = ROOT / "upstream/src/main/resources/assets/enigmaticlegacy/lang"
PORT = ROOT / VERSION / "src/main/resources/assets/enigmaticlegacy/lang"


def load(path):
    # Upstream lang files hold a few duplicate keys; the last one wins, as in the game.
    return json.loads(path.read_text(encoding="utf-8"))


out = ["# Lang comparison with upstream", ""]
for up in sorted(UPSTREAM.glob("*.json")):
    port = PORT / up.name
    if not port.exists():
        out.append(f"## {up.name}: **missing in the port**")
        continue
    a, b = load(up), load(port)
    missing = [k for k in a if k not in b]
    added = [k for k in b if k not in a]
    changed = [k for k in a if k in b and a[k] != b[k]]
    out.append(f"## {up.name}: {len(missing)} missing, {len(added)} added, {len(changed)} changed")
    for title, keys, show in (("Missing", missing, a), ("Added (port-only)", added, b), ("Changed", changed, None)):
        if not keys:
            continue
        out.append(f"### {title}")
        for k in keys:
            if show is None:
                out.append(f"- `{k}`: `{a[k]}` -> `{b[k]}`")
            else:
                out.append(f"- `{k}`: `{show[k]}`")
    out.append("")

dest = ROOT / "test-output/playtest/lang.md"
dest.parent.mkdir(parents=True, exist_ok=True)
dest.write_text("\n".join(out) + "\n", encoding="utf-8")
print("\n".join(line for line in out if line.startswith("## ")))
