# UI Responsiveness Improvement Plan

The user reported that the UI elements (especially buttons) are poorly positioned on different phone models. Research shows that many layouts use hardcoded heights, large fixed text sizes, and lack `ScrollView` wrappers, which causes content to overflow or be cut off on smaller screens.

## Proposed Changes

### 1. Standardizing Dimensions and Text Sizes

I will introduce `dimens.xml` to centralize dimensions and provide overrides for smaller screens (e.g., phones with low height).

#### [NEW] [dimens.xml](file:///C:/WordCrushMobil/app/src/main/res/values/dimens.xml)

- Define standard button heights, text sizes, and margins.

#### [NEW] [dimens.xml (small)](file:///C:/WordCrushMobil/app/src/main/res/values-small/dimens.xml)

- Provide smaller values for screens with limited real estate.

---

### 2. Layout Enhancements

#### [activity_main.xml](file:///C:/WordCrushMobil/app/src/main/res/layout/activity_main.xml)

- Wrap the `ConstraintLayout` in a `ScrollView` with `fillViewport="true"`.
- Use `dimen` resources for text sizes and button heights.

#### [activity_menu.xml](file:///C:/WordCrushMobil/app/src/main/res/layout/activity_menu.xml)

- Wrap the `ConstraintLayout` in a `ScrollView` with `fillViewport="true"`.
- Use `dimen` resources for text sizes and button heights.

#### [activity_secim_ekrani.xml](file:///C:/WordCrushMobil/app/src/main/res/layout/activity_secim_ekrani.xml)

- Convert root to `ScrollView`.
- Use weights more effectively to handle vertical space.

#### [activity_skor_tablosu.xml](file:///C:/WordCrushMobil/app/src/main/res/layout/activity_skor_tablosu.xml)

- Adjust `ScrollView` and summary panel weights to ensure the list remains visible and usable.

---

### 3. Dynamic Layout Logic Improvements

#### [OyunEkraniActivity.kt](file:///C:/WordCrushMobil/app/src/main/java/tr/edu/kocaeli/wordcrush/OyunEkraniActivity.kt)

- Update `gridiCiz` to calculate `hucreBoyutu` based on both available width AND estimated available height.
- This prevents the grid from pushing bottom elements off-screen on short devices.

## Verification Plan

### Automated Tests
- I will run `gradle assembleDebug` to ensure no layout errors were introduced.

### Manual Verification
- I will use `ui_state` and `take_screenshot` to verify the main screens on the current device.
- I will specifically check if the "Start Game" buttons are visible and clickable.
- I will verify that `ScrollView` allows reaching all elements if they don't fit the screen.
