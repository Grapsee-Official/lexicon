"""Cross-check our C++ NFC/NFD/casefold against Python's unicodedata."""
import unicodedata
import subprocess
import sys

proc = subprocess.Popen(['/tmp/driver'], stdin=subprocess.PIPE, stdout=subprocess.PIPE)

def call(op, s):
    b = s.encode('utf-8')
    assert len(b) < 256
    proc.stdin.write(bytes([ord(op), len(b)]) + b)
    proc.stdin.flush()
    out_len = proc.stdout.read(1)[0]
    return proc.stdout.read(out_len).decode('utf-8')

# Comprehensive test cases
test_strings = [
    "ÀÉÎÕÜàéîõüÿ", "café", "über", "naïve", "résumé",
    "ĀčĒĥĪļŊōŘşťūŽ",
    "ǍǐǑǔȔȳȤȴ",
    "tiếng Việt", "tự", "lời", "chữ", "Phở", "người",
    unicodedata.normalize('NFD', "tiếng Việt"),
    unicodedata.normalize('NFD', "tự"),
    unicodedata.normalize('NFD', "người"),
    "Άέήίόύώϊϋΐΰ",
    "ὀνομαστική",
    "ἀνθρωπος",
    "ῥήτορος",
    "Ёешь", "Привет", "наука", "объект",
    "文字", "字典", "搜尋",
    "豈更車賈",
    "Hello, 世界!", "résumé-CV",
    "",
    "ASCII only", "1234567890", "MixedCase123",
    "ß", "İ", "ı", "ﬁ", "ﬂ", "Ω",
    "Straße", "Größe", "Müller",
    "Donaudampfschifffahrtselektrizitätenhauptbetriebswerkbauunterbeamtengesellschaft",
    # Greek polytonic uppercase
    "ἈΝΘΡΩΠΟΣ",
    # CJK compat singletons
    "豈更車",  # U+FA00-FA02
    # Korean
    "한국어", "안녕하세요",
    unicodedata.normalize('NFD', "한국어"),
]

failures = 0
total = 0
for s in test_strings:
    for op, fn_name in [('C', 'NFC'), ('D', 'NFD'), ('F', 'casefold'), ('S', 'strip_diacritics')]:
        total += 1
        if op == 'C': expected = unicodedata.normalize('NFC', s)
        elif op == 'D': expected = unicodedata.normalize('NFD', s)
        elif op == 'F': expected = s.casefold()
        elif op == 'S':
            nfd = unicodedata.normalize('NFD', s)
            expected = ''.join(c for c in nfd if not unicodedata.combining(c))
        actual = call(op, s)
        if actual != expected:
            failures += 1
            print(f"FAIL [{fn_name}] input={s!r}")
            print(f"  expected: {expected!r} ({expected.encode('utf-8').hex(' ')})")
            print(f"  actual:   {actual!r}   ({actual.encode('utf-8').hex(' ')})")
print(f"\n{total-failures}/{total} passed, {failures} failed.")
sys.exit(1 if failures else 0)
