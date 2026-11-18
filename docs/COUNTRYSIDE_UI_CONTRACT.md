# Lexicon countryside native UI contract

The owner's nine watercolor UI screenshots are the design source of truth. Keep real English countryside painting, honey-stone cottages, old bridges, floral sprigs, warm parchment, sage/forest ink and readable literary typography.

The ten native destinations are Dictionary Home, Word of the Day, Word Detail, Collections, My Notes, Study Lists, Quick Templates, Search & Discovery, Search Results and Expanded Exploration. These are implemented in native Kotlin/Jetpack Compose and Swift/SwiftUI.

Always preserve the floating pill navigation: Home, Dictionary, raised Search, Collections, More. Light and dark illustration versions must exist on both platforms; scripts/check_countryside_art.py enforces filename parity. Complex existing watercolor paintings are raster; the native botanical strokes are editable code shapes, not falsely labelled editable SVG files.

Build policy: only main, Android Release (never debug), iOS Release simulator compilation, no automated screenshot audit. Publish the same signed APK asset to the tag lexicon-latest after both builds pass. A stable Android signing keystore must be supplied through Actions secrets. iOS simulator compilation is not signed device distribution; the owner handles visual verification.
