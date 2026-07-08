import { StyleSheet } from 'react-native';

import { Body, Caption, Screen, Title } from '@/shared/components/ui';
import { colors, spacing } from '@/shared/theme';

export default function CheckInPendingScreen() {
  return (
    <Screen style={styles.container}>
      <Title>En attente</Title>
      <Caption>Ton check-in attend la validation d'un autre Quester.</Caption>
      <Body style={styles.status}>⏳ Statut : pending</Body>
    </Screen>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: spacing.md,
  },
  status: {
    marginTop: spacing.xl,
    fontSize: 18,
    color: colors.warning,
    fontWeight: '600',
  },
});
