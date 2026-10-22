"""Copy upstream Enigmatic Legacy resources into a Fabric port project.

Usage: py -3 tools/sync_resources.py 1.20.1

Copies upstream/src/main/resources/{assets,data} into <version>/src/main/resources,
converting Forge-only data for Fabric. Every conversion here is listed in CHANGES.md:

- data/curios/tags/items/<slot>.json -> data/trinkets/tags/items/<group>/<slot>.json
  (slot mapping in SLOT_MAP; slot definitions are hand-written, not generated here)
- recipe "conditions" (Forge) -> "fabric:load_conditions" (Fabric API)
- data/forge/** and Forge global loot modifier JSON are skipped (ported in code)

Re-running overwrites copied files but never deletes extra files in the target.
"""
import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "upstream" / "src" / "main" / "resources"

# Curios slot -> Trinkets slots (group/slot). See CHANGES.md "Accessory slots".
SLOT_MAP = {
    "charm": ["chest/charm"],
    "ring": ["hand/ring", "offhand/ring"],
    "back": ["chest/back"],
    "spellstone": ["legs/spellstone"],
    "scroll": ["head/scroll"],
}

SKIP_DIRS = [
    Path("data/curios"),
    Path("data/forge"),
    Path("data/enigmaticlegacy/loot_modifiers"),
]

COND_KEY = re.compile(r'"conditions"(\s*):(\s*)\[')
COND_TYPE = re.compile(r'"type"(\s*):(\s*)"enigmaticlegacy:is_enabled"')


def skipped(rel: Path) -> bool:
    return any(rel.is_relative_to(d) for d in SKIP_DIRS)


def convert_recipe(text: str) -> str:
    if '"conditions"' not in text:
        return text
    text = COND_KEY.sub(r'"fabric:load_conditions"\1:\2[', text, count=1)
    return COND_TYPE.sub(r'"condition"\1:\2"enigmaticlegacy:is_enabled"', text)


def main(version: str) -> None:
    dst_root = ROOT / version / "src" / "main" / "resources"
    copied = converted = 0

    for top in ("assets", "data"):
        for src in sorted((SRC / top).rglob("*")):
            if not src.is_file():
                continue
            rel = src.relative_to(SRC)
            if skipped(rel):
                continue
            dst = dst_root / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            if rel.is_relative_to(Path("data/enigmaticlegacy/recipes")) and src.suffix == ".json":
                text = src.read_text(encoding="utf-8")
                new = convert_recipe(text)
                json.loads(new)  # fail loudly on a bad conversion
                converted += new != text
                dst.write_text(new, encoding="utf-8", newline="")
            else:
                shutil.copyfile(src, dst)
            copied += 1

    for slot, targets in SLOT_MAP.items():
        tag = json.loads((SRC / "data/curios/tags/items" / f"{slot}.json").read_text(encoding="utf-8"))
        for target in targets:
            dst = dst_root / "data/trinkets/tags/items" / f"{target}.json"
            dst.parent.mkdir(parents=True, exist_ok=True)
            dst.write_text(json.dumps({"replace": False, "values": tag["values"]}, indent=2) + "\n", encoding="utf-8")

    print(f"{copied} files copied, {converted} recipes converted, {sum(map(len, SLOT_MAP.values()))} trinket tags")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "1.20.1")
