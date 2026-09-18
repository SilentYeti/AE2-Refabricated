#!/usr/bin/env python3
"""
Where the Fabric port actually stands, measured from the tree.

Run from the repository root:  python3 tools/parity_report.py [--markdown]

Nothing here is hand-maintained, because hand-maintained progress numbers drift. Everything is
counted from the sources, so a stale report is an impossible state rather than a likely one.

Two of the three coupling axes are invisible here -- access transformers, and methods NeoForge
patches onto vanilla classes -- so treat "blocked by" as a lower bound. The only exact measure is
moving files into :common and compiling; see MULTILOADER.md.
"""
import argparse
import collections
import os
import re
import sys

COMMON = "common/src/main/java"
NEOFORGE = "neoforge/src/main/java"
NEOFORGE_CLIENT = "neoforge/src/client/java"

THIRD_PARTY = {
    "guideme.": "guideme", "mezz.jei.": "jei", "snownee.jade.": "jade",
    "dev.emi.": "emi", "me.shedaniel.": "rei", "mcp.mobius.": "wthit",
}
NEOFORGE_GROUPS = [
    "neoforge.transfer", "neoforge.capabilities", "neoforge.registries", "neoforge.network",
    "neoforge.model", "neoforge.client", "neoforge.common", "neoforge.event", "neoforge.fluids",
    "neoforge.attachment", "neoforge.server", "neoforge.energy", "neoforge.items",
]


def java_files(root):
    out = []
    for dirpath, _, names in os.walk(root):
        # Forward slashes throughout: the rest of this script splits on "/java/", and on Windows
        # os.walk hands back backslashes.
        out.extend(os.path.join(dirpath, n).replace(os.sep, "/") for n in names if n.endswith(".java"))
    return out


def group_of(imp):
    if imp.startswith("net.neoforged."):
        tail = imp[len("net.neoforged."):]
        for g in NEOFORGE_GROUPS:
            if tail.startswith(g):
                return g.replace("neoforge.", "")
        if tail.startswith(("bus.", "fml.", "api.dist", "neoforgespi")):
            return "fml/bus"
        return "neoforge.other"
    for prefix, name in THIRD_PARTY.items():
        if imp.startswith(prefix):
            return name
    return None


def analyse():
    roots = [r for r in (COMMON, NEOFORGE) if os.path.isdir(r)]
    files, owner = [], {}
    for root in roots:
        for path in java_files(root):
            files.append(path)
            owner[path.split("/java/", 1)[1][:-5].replace("/", ".")] = path

    own, deps = {}, {}
    for path in files:
        src = open(path, errors="replace").read()
        imports = re.findall(r"^import\s+(?:static\s+)?([\w.]+)", src, re.M)
        own[path] = {g for g in (group_of(i) for i in imports) if g}
        deps[path] = {owner[i] for i in imports if i in owner}

    sys.setrecursionlimit(30000)
    memo = {}

    def blockers(path, stack=()):
        if path in memo:
            return memo[path]
        if path in stack:
            return set()
        acc = set(own[path])
        for dep in deps[path]:
            acc |= blockers(dep, stack + (path,))
        memo[path] = acc
        return acc

    in_neoforge = [p for p in files if p.startswith(NEOFORGE)]
    profile = collections.Counter()
    invisible = 0
    for path in in_neoforge:
        found = frozenset(blockers(path))
        if found:
            profile[found] += 1
        else:
            invisible += 1
    return files, in_neoforge, profile, invisible


def greedy(profile, limit=10):
    groups = set().union(*[set(b) for b in profile]) if profile else set()
    chosen, rows = set(), []
    for _ in range(min(limit, len(groups))):
        best = None
        for g in groups - chosen:
            freed = sum(n for bs, n in profile.items() if bs <= chosen | {g})
            if best is None or freed > best[0]:
                best = (freed, g)
        if best is None:
            break
        chosen.add(best[1])
        rows.append((best[1], best[0]))
    return rows


def ratchet_counts():
    """Declarations in the ratchet. Not the registered count -- some declarations expand into
    several items (the coloured variants), so the authoritative number is what the client gametest
    reports. This is only here to show the ratchet is being kept up."""
    path = os.path.join(COMMON, "appeng/core/definitions/AECommonItems.java")
    if not os.path.isfile(path):
        return None, None
    src = open(path, errors="replace").read()
    ported = len(re.findall(r"^\s+item\(", src, re.M))
    deferred = len(re.findall(r"Map\.entry\(", src))
    return ported, deferred


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--markdown", action="store_true", help="emit a markdown table")
    args = ap.parse_args()

    if not os.path.isdir(COMMON):
        sys.exit("run this from the repository root")

    files, in_neoforge, profile, invisible = analyse()
    common_count = len(files) - len(in_neoforge)
    client_count = len(java_files(NEOFORGE_CLIENT)) if os.path.isdir(NEOFORGE_CLIENT) else 0
    visible = sum(profile.values())
    ported_items, deferred_items = ratchet_counts()

    bullet = "| %s | %s |" if args.markdown else "  %-34s %s"
    head = (lambda t: print(f"\n## {t}\n\n| | |\n|---|---|")) if args.markdown else (lambda t: print(f"\n{t}"))

    print("# Fabric port status" if args.markdown else "Fabric port status")

    head("Where the code lives")
    print(bullet % (":common (loader-agnostic)", common_count))
    print(bullet % (":neoforge src/main", len(in_neoforge)))
    print(bullet % (":neoforge src/client", client_count))
    if ported_items is not None:
        print(bullet % ("item declarations ported", ported_items))
        print(bullet % ("item declarations deferred", deferred_items))

    head("Why the rest is stuck")
    print(bullet % ("blocked by an import", visible))
    print(bullet % ("blocked invisibly (ATs, patches)", invisible))

    head("Order of attack (cumulative files freed)")
    if args.markdown:
        print()
        print("| after abstracting | files freed of %d |" % visible)
        print("|---|---|")
    for group, freed in greedy(profile):
        print(("| `%s` | %d |" % (group, freed)) if args.markdown
              else "  %-34s %d" % ("+ " + group, freed))

    head("Biggest single blockers")
    singles = collections.Counter()
    for found, n in profile.items():
        if len(found) == 1:
            singles[next(iter(found))] += n
    if args.markdown:
        print()
        print("| sole blocker | files |")
        print("|---|---|")
    for group, n in singles.most_common(8):
        print(("| `%s` | %d |" % (group, n)) if args.markdown
              else "  %-34s %d" % (group, n))
    print()


if __name__ == "__main__":
    main()
