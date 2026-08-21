#!/usr/bin/env python3
"""Assemble the new unicode.cpp by replacing the decomposition_table and
composition_table bodies with expanded versions generated from unicodedata."""
import unicodedata
import sys

SRC = "/home/z/my-project/native/cpp/src/unicode.cpp"

# ---- read original ----
with open(SRC, "r", encoding="utf-8") as f:
    original = f.read()

# ---- collect decomposition entries ----
RANGES = [
    ("Latin Extended-A",          0x0100, 0x017F),
    ("Latin Extended-B",          0x0180, 0x024F),
    ("Latin Extended Additional", 0x1E00, 0x1EFF),
    ("Greek and Coptic",          0x0370, 0x03FF),
    ("Cyrillic",                  0x0400, 0x04FF),
]

def decomposition_pair(cp):
    decomp = unicodedata.decomposition(chr(cp))
    if not decomp or decomp.startswith('<'):
        return None
    parts = decomp.split()
    if len(parts) != 2:
        return None
    try:
        return (int(parts[0], 16), int(parts[1], 16))
    except ValueError:
        return None

decomposition_entries = []  # (cp, base, comb)
for label, lo, hi in RANGES:
    for cp in range(lo, hi + 1):
        pair = decomposition_pair(cp)
        if pair is None:
            continue
        decomposition_entries.append((cp, pair[0], pair[1]))

# ---- build composition entries grouped by base ----
by_base = {}
for (cp, b, c) in decomposition_entries:
    by_base.setdefault(b, []).append((c, cp))

# ---- emit decomposition_table block ----
def emit_decomp_block():
    lines = []
    for label, lo, hi in RANGES:
        lines.append(f"    // {label} (U+{lo:04X}..U+{hi:04X})")
        in_range = [(cp, b, c) for (cp, b, c) in decomposition_entries
                    if lo <= cp <= hi]
        for i in range(0, len(in_range), 3):
            chunk = in_range[i:i+3]
            parts = [f"{{0x{cp:04X}, {{0x{b:04X}, 0x{c:04X}}}}}"
                     for (cp, b, c) in chunk]
            line = "    " + ", ".join(parts)
            if i + 3 < len(in_range):
                line += ","
            else:
                # Last line of this range — needs a trailing comma if more
                # ranges follow, since this is inside the initializer list.
                line += ","
            lines.append(line)
        lines.append("")  # blank line between ranges
    # Drop the trailing blank line and remove the very last comma so the
    # closing `};` follows a clean line. (The last entry's line currently
    # ends with ",")
    while lines and lines[-1] == "":
        lines.pop()
    # Remove trailing comma on the final non-empty line
    for j in range(len(lines) - 1, -1, -1):
        if lines[j].rstrip().endswith(","):
            lines[j] = lines[j].rstrip()[:-1]
            break
    return "\n".join(lines)

def emit_composition_block():
    lines = []
    for base in sorted(by_base.keys()):
        items = sorted(by_base[base])
        parts = [f"{{0x{base:04X}0000 + 0x{c:04X}, 0x{cp:04X}}}"
                 for (c, cp) in items]
        for i in range(0, len(parts), 3):
            chunk = parts[i:i+3]
            line = "    " + ", ".join(chunk)
            # All lines except the very last overall need a trailing comma.
            line += ","
            lines.append(line)
    # Remove trailing comma on the final line
    if lines:
        lines[-1] = lines[-1].rstrip()[:-1]
    return "\n".join(lines)

decomp_block = emit_decomp_block()
composition_block = emit_composition_block()

# ---- find the two table bodies and replace ----
# The decomposition_table runs from the line containing `// Latin-1 Supplement`
# (first occurrence inside decomposition_table) to the line containing
# `{0x1EF8, {0x0059, 0x0303}}, {0x1EF9, {0x0079, 0x0303}}` then `};`.

# Strategy: locate the markers and rebuild the file by string substitution
# on the well-known anchor texts.

# --- Replace decomposition body ---
# Anchor: from `    // Latin Extended-A (common European)` up to and including
# the final Vietnamese line `{0x1EF8, {0x0059, 0x0303}}, {0x1EF9, {0x0079, 0x0303}},`
DECOMP_START = "    // Latin Extended-A (common European)\n"
DECOMP_END = "    {0x1EF8, {0x0059, 0x0303}}, {0x1EF9, {0x0079, 0x0303}},\n"
assert DECOMP_START in original, "decomp start anchor not found"
assert DECOMP_END in original, "decomp end anchor not found"
start_idx = original.index(DECOMP_START)
end_idx = original.index(DECOMP_END) + len(DECOMP_END)

new_decomp_section = decomp_block + "\n"
new_original = original[:start_idx] + new_decomp_section + original[end_idx:]

# --- Replace composition body ---
# Anchor: from `    // Vietnamese (key examples)\n` up to and including
# the final Vietnamese line ending `0x1EF5},\n`
COMP_START = "    // Vietnamese (key examples)\n"
COMP_END = "    {0x00790000 + 0x0323, 0x1EF5},\n"
assert COMP_START in new_original, "comp start anchor not found"
assert COMP_END in new_original, "comp end anchor not found"
cstart = new_original.index(COMP_START)
cend = new_original.index(COMP_END) + len(COMP_END)

new_comp_section = (
    "    // Latin Extended-A, B, Additional (incl. Vietnamese), Greek, Cyrillic\n"
    + composition_block + "\n"
)
final = new_original[:cstart] + new_comp_section + new_original[cend:]

# ---- update the header comments to reflect expanded coverage ----
final = final.replace(
    "// Canonical decomposition table (common Latin diacritics)\n"
    "// Full table would be in unicode_data.hpp — this covers the most common cases\n",
    "// Canonical decomposition table — Latin (incl. Vietnamese), Greek, Cyrillic.\n"
    "// Generated from Unicode 15.0 canonical decompositions (close to 15.1).\n"
    "// Only entries that decompose to exactly two code points (base + single\n"
    "// combining mark) are listed; multi-step decomposition is performed in\n"
    "// normalize_nfd() via lookup of intermediate precomposed characters.\n",
)
final = final.replace(
    "// Composition table (reverse of decomposition) — for NFC\n",
    "// Composition table (reverse of decomposition) — for NFC.\n"
    "// Generated alongside the decomposition table; covers Latin Extended-A/B,\n"
    "// Latin Extended Additional (Vietnamese), Greek, and Cyrillic.\n",
)

with open(SRC, "w", encoding="utf-8") as f:
    f.write(final)

# ---- stats ----
print(f"decomposition entries: {len(decomposition_entries)}", file=sys.stderr)
print(f"composition entries:   {len(by_base)} bases, "
      f"{sum(len(v) for v in by_base.values())} total",
      file=sys.stderr)
print(f"file written: {SRC}", file=sys.stderr)
