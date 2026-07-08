export const colors = {
  background: '#0F0F12',
  surface: '#1A1A22',
  surfaceElevated: '#242430',
  border: '#2E2E3A',
  text: '#F5F5F7',
  textMuted: '#8E8E9A',
  primary: '#6C5CE7',
  primaryMuted: '#5A4BD1',
  accent: '#00CEC9',
  success: '#00B894',
  warning: '#FDCB6E',
  error: '#FF6B6B',
  rarity: {
    common: '#8E8E9A',
    rare: '#6C5CE7',
    legendary: '#FDCB6E',
  },
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
} as const;

export const radius = {
  sm: 8,
  md: 12,
  lg: 16,
  full: 999,
} as const;

export const typography = {
  title: { fontSize: 28, fontWeight: '700' as const },
  heading: { fontSize: 20, fontWeight: '600' as const },
  body: { fontSize: 16, fontWeight: '400' as const },
  caption: { fontSize: 13, fontWeight: '400' as const },
};
