import { StyleSheet, Text, View } from 'react-native';

import { RARITY_LABELS, type Rarity } from '@quest/shared';

import { colors, radius, spacing, typography } from '@/shared/theme';

interface RarityBadgeProps {
  rarity: Rarity;
}

export function RarityBadge({ rarity }: RarityBadgeProps) {
  return (
    <View style={[styles.badge, { backgroundColor: colors.rarity[rarity] + '22' }]}>
      <Text style={[styles.label, { color: colors.rarity[rarity] }]}>
        {RARITY_LABELS[rarity]}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  badge: {
    alignSelf: 'flex-start',
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs,
    borderRadius: radius.full,
  },
  label: {
    ...typography.caption,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
});
