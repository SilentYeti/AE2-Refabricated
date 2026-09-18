#!/usr/bin/env python3
"""
Move files into :common and keep whatever compiles.

    python3 tools/try_move_to_common.py appeng/recipes            # a package, recursively
    python3 tools/try_move_to_common.py appeng/block/AEBaseBlock.java
    python3 tools/try_move_to_common.py --all                     # everything in :neoforge/main
    python3 tools/try_move_to_common.py appeng/recipes --dry-run  # report, change nothing

This is the only reliable way to find what can live in :common, because two of the three coupling
axes are invisible to reading imports:

  1. imports of net.neoforged.*            -- visible
  2. access transformers                   -- invisible; AE2 widens 56 vanilla members and :common
                                              has none, so anything touching one fails to compile
  3. methods NeoForge patches onto vanilla -- invisible; called with no NeoForge import at all

So: move the candidates, compile, move back whatever failed, repeat until it settles. Files that
bounce are reported with the first compiler error, which is the actual reason they cannot move.

Run from the repository root with JAVA_HOME pointing at JDK 25. Nothing is committed; check
`git status` and run the rest of the loop in PORTING.md before you do.
"""
import argparse
import os
import re
import shutil
import subprocess
import sys

COMMON = "common/src/main/java"
NEOFORGE = "neoforge/src/main/java"
MAX_ROUNDS = 12


def candidates(target):
    """Files under :neoforge/main matching a package prefix or an exact path."""
    if target == "--all":
        root = NEOFORGE
    else:
        root = os.path.join(NEOFORGE, target)
    if os.path.isfile(root):
        return [root]
    if not os.path.isdir(root):
        sys.exit(f"no such package or file under {NEOFORGE}: {target}")
    out = []
    for dirpath, _, names in os.walk(root):
        out.extend(os.path.join(dirpath, n) for n in names if n.endswith(".java"))
    return sorted(out)


def move(src, from_root, to_root):
    rel = src[len(from_root) + 1:]
    dst = os.path.join(to_root, rel)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.move(src, dst)
    return dst


def compile_common():
    """Returns (ok, {relative path: first error line})."""
    result = subprocess.run(
        ["./gradlew", ":common:compileJava", "--no-daemon", "-q", "--console=plain"],
        capture_output=True, text=True)
    if result.returncode == 0:
        return True, {}
    log = result.stdout + result.stderr
    failures = {}
    for match in re.finditer(r'/' + COMMON + r'/(appeng/[^:]+\.java):(\d+): error: (.*)', log):
        failures.setdefault(match.group(1), f"{match.group(3).strip()} (line {match.group(2)})")
    if not failures:
        sys.stderr.write(log[-4000:])
        sys.exit("`:common:compileJava` failed without naming a file in :common -- see above. "
                 "Something other than the move is broken; nothing was left moved.")
    return False, failures


def prune_empty_dirs(root):
    for dirpath, _, _ in sorted(os.walk(root), reverse=True):
        if os.path.isdir(dirpath) and not os.listdir(dirpath):
            os.rmdir(dirpath)


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("target", help="package prefix under appeng/, a single .java path, or --all")
    ap.add_argument("--dry-run", action="store_true",
                    help="move, compile, report, then put everything back")
    args = ap.parse_args()

    if not os.path.isdir(COMMON):
        sys.exit("run this from the repository root")

    files = candidates(args.target)
    if not files:
        sys.exit("nothing to move")
    print(f"moving {len(files)} file(s) into :common\n")

    moved = [move(f, NEOFORGE, COMMON) for f in files]
    prune_empty_dirs(NEOFORGE)

    bounced = {}
    for round_no in range(1, MAX_ROUNDS + 1):
        ok, failures = compile_common()
        if ok:
            print(f"round {round_no}: :common compiles")
            break
        back = 0
        for rel, reason in failures.items():
            src = os.path.join(COMMON, rel)
            if not os.path.exists(src):
                continue
            move(src, COMMON, NEOFORGE)
            bounced[rel] = reason
            back += 1
        print(f"round {round_no}: moved {back} back")
        if back == 0:
            sys.exit("no progress -- the errors are in files that are not being moved; "
                     "fix them by hand, or widen the target")
    else:
        sys.exit(f"did not settle in {MAX_ROUNDS} rounds")

    prune_empty_dirs(COMMON)
    kept = [m[len(COMMON) + 1:] for m in moved if os.path.exists(m)]

    if args.dry_run:
        for rel in kept:
            move(os.path.join(COMMON, rel), COMMON, NEOFORGE)
        prune_empty_dirs(COMMON)
        print("\n(dry run: everything put back)")

    print(f"\nstayed in :common : {len(kept)}")
    for rel in sorted(kept):
        print(f"  {rel}")
    print(f"\nbounced back      : {len(bounced)}")
    for rel, reason in sorted(bounced.items()):
        print(f"  {rel}\n      {reason}")

    if not args.dry_run and kept:
        print("\nNow run the rest of the loop before committing:")
        print("  ./gradlew :neoforge:test          # nothing regressed")
        print("  ./gradlew build                   # all three modules")
        print("  ./gradlew :fabric:runClientGametest")


if __name__ == "__main__":
    main()
