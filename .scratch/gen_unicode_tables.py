#!/usr/bin/env python3
"""
Generate decomposition_table and composition_table entries for the requested
Unicode ranges, using Python's unicodedata (Unicode 15.0, close to 15.1).

A decomposition entry is added ONLY when the canonical decomposition yields
exactly two code points (i.e. precomposed char -> base + single combining mark).
Compatibility decompositions (prefixed with '<...>') are skipped.

Output is C++ initializer-list syntax ready to paste into unicode.cpp.
"""
import unicodedata
import sys

def is_common_greek(cp):
    return True

def is_common_cyrillic(cp):
    return True

RANGES = [
    ("Latin Extended-A",              0x0100, 0x017F, None),
    ("Latin Extended-B",              0x0180, 0x024F, None),
    ("Latin Extended Additional",     0x1E00, 0x1EFF, None),
    ("Greek and Coptic",              0x0370, 0x03FF, is_common_greek),
    ("Cyrillic",                      0x0400, 0x04FF, is_common_cyrillic),
]

def decomposition_pair(cp):
    decomp = unicodedata.decomposition(chr(cp))
    if not decomp:
        return None
    if decomp.startswith('<'):
        return None
    parts = decomp.split()
    if len(parts) != 2:
        return None
    try:
        a = int(parts[0], 16)
        b = int(parts[1], 16)
    except ValueError:
        return None
    return (a, b)

def main():
    decomposition_entries = []
    for label, lo, hi, filt in RANGES:
        for cp in range(lo, hi + 1):
            if filt and not filt(cp):
                continue
            pair = decomposition_pair(cp)
            if pair is None:
                continue
            decomposition_entries.append((cp, pair[0], pair[1]))

    composition_map = {}
    for cp, base, comb in decomposition_entries:
        key = (base << 16) | comb
        composition_map[key] = cp

    print("// ===== Generated decomposition_table additions =====")
    for label, lo, hi, filt in RANGES:
        print(f"    // {label} (U+{lo:04X}..U+{hi:04X})")
        in_range = [(cp, b, c) for (cp, b, c) in decomposition_entries
                    if lo <= cp <= hi]
        for i in range(0, len(in_range), 3):
            chunk = in_range[i:i+3]
            parts = []
            for (cp, b, c) in chunk:
                parts.append(f"{{0x{cp:04X}, {{0x{b:04X}, 0x{c:04X}}}}}")
            line = "    " + ", ".join(parts)
            if i + 3 < len(in_range):
                line += ","
            print(line)
        print()

    print("// ===== Generated composition_table additions =====")
    by_base = {}
    for (cp, b, c) in decomposition_entries:
        by_base.setdefault(b, []).append((c, cp))
    for base in sorted(by_base.keys()):
        items = sorted(by_base[base])
        line_parts = []
        for (c, cp) in items:
            line_parts.append(f"{{0x{base:04X}0000 + 0x{c:04X}, 0x{cp:04X}}}")
        for i in range(0, len(line_parts), 3):
            chunk = line_parts[i:i+3]
            line = "    " + ", ".join(chunk)
            if i + 3 < len(line_parts):
                line += ","
            print(line)

    print(f"\n// Total decomposition entries: {len(decomposition_entries)}",
          file=sys.stderr)
    print(f"// Total composition entries:   {len(composition_map)}",
          file=sys.stderr)

if __name__ == "__main__":
    main()
