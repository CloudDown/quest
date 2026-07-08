import { StyleSheet, Text, type TextProps, View, type ViewProps } from 'react-native';

import { colors, radius, spacing, typography } from '@/shared/theme';

export function Screen({ style, ...props }: ViewProps) {
  return <View style={[styles.screen, style]} {...props} />;
}

export function Card({ style, ...props }: ViewProps) {
  return <View style={[styles.card, style]} {...props} />;
}

export function Title({ style, ...props }: TextProps) {
  return <Text style={[styles.title, style]} {...props} />;
}

export function Body({ style, ...props }: TextProps) {
  return <Text style={[styles.body, style]} {...props} />;
}

export function Caption({ style, ...props }: TextProps) {
  return <Text style={[styles.caption, style]} {...props} />;
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.background,
    padding: spacing.md,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: radius.md,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  title: {
    ...typography.title,
    color: colors.text,
  },
  body: {
    ...typography.body,
    color: colors.text,
  },
  caption: {
    ...typography.caption,
    color: colors.textMuted,
  },
});
