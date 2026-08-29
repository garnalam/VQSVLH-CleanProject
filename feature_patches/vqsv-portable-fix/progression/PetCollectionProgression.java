package vqsv.progression;

import java.util.Arrays;
import java.util.function.IntPredicate;
import vqsv.pet.data.LietHoaMutationCatalog;
import vqsv.pet.data.ThienLuanMutationCatalog;
import vqsv.pet.data.HoaDiemHauVuongCatalog;
import vqsv.pet.data.ThienViemHoCatalog;

public final class PetCollectionProgression {
   public static final byte UNSEEN = 0;
   public static final byte SEEN = 1;
   public static final byte COLLECTED = 2;
   public static final int SPECIES_COUNT = 202;
   public static final int LIET_HOA_SPECIES_COUNT = 100;
   public static final int V4_SPECIES_BASE = 202;
   public static final int V4_SPECIES_COUNT = 72;
   public static final int MUTATION_SPECIES_BASE = 274;
   public static final int MUTATION_SPECIES_COUNT = 4;
   private static final int[] CONVENIENCE_REWARD_MILESTONES = new int[]{10, 15, 20, 30, 40, 50, 100};
   private final byte[] states = new byte[202];
   private final byte[] v4States = new byte[72];
   private final byte[] mutationStates = new byte[4];
   private final boolean[] convenienceRewardsClaimed;

   public PetCollectionProgression() {
      this.convenienceRewardsClaimed = new boolean[CONVENIENCE_REWARD_MILESTONES.length];
   }

   public byte status(int var1) {
      int var2 = mutationIndex(var1);
      if (var2 >= 0) {
         return this.mutationStates[var2];
      } else if (validSpecies(var1)) {
         return this.states[var1];
      } else {
         return validV4Species(var1) ? this.v4States[var1 - 202] : 0;
      }
   }

   public boolean seen(int var1) {
      return this.status(var1) >= 1;
   }

   public boolean collected(int var1) {
      return this.status(var1) == 2;
   }

   public boolean markSeen(int var1) {
      int var2 = mutationIndex(var1);
      if (var2 >= 0) {
         if (this.mutationStates[var2] >= 1) {
            return false;
         } else {
            this.mutationStates[var2] = 1;
            return true;
         }
      } else if (validSpecies(var1)) {
         if (this.states[var1] >= 1) {
            return false;
         } else {
            this.states[var1] = 1;
            return true;
         }
      } else if (validV4Species(var1) && this.v4States[var1 - 202] < 1) {
         this.v4States[var1 - 202] = 1;
         return true;
      } else {
         return false;
      }
   }

   public boolean markCollected(int var1) {
      int var2 = mutationIndex(var1);
      if (var2 >= 0) {
         if (this.mutationStates[var2] == 2) {
            return false;
         } else {
            this.mutationStates[var2] = 2;
            return true;
         }
      } else if (validSpecies(var1)) {
         if (this.states[var1] == 2) {
            return false;
         } else {
            this.states[var1] = 2;
            return true;
         }
      } else if (validV4Species(var1) && this.v4States[var1 - 202] != 2) {
         this.v4States[var1 - 202] = 2;
         return true;
      } else {
         return false;
      }
   }

   public int v4CollectedSpeciesCount() {
      int var1 = 0;

      for(byte var5 : this.v4States) {
         if (var5 == 2) {
            ++var1;
         }
      }

      return var1;
   }

   public int collectedSpeciesCount() {
      return this.countCollected((var0) -> true);
   }

   public int lietHoaCollectedSpeciesCount() {
      return this.countCollected((var0) -> var0 < 100);
   }

   public int countCollected(IntPredicate var1) {
      int var2 = 0;

      for(int var3 = 0; var3 < this.states.length; ++var3) {
         if (this.states[var3] == 2 && var1.test(var3)) {
            ++var2;
         }
      }

      return var2;
   }

   public byte[] snapshot() {
      byte[] var1 = new byte[278];
      System.arraycopy(this.states, 0, var1, 0, this.states.length);
      System.arraycopy(this.v4States, 0, var1, 202, this.v4States.length);
      System.arraycopy(this.mutationStates, 0, var1, 274, this.mutationStates.length);
      return var1;
   }

   public int nextConvenienceRewardTier() {
      for(int var1 = 0; var1 < this.convenienceRewardsClaimed.length; ++var1) {
         if (!this.convenienceRewardsClaimed[var1]) {
            return var1;
         }
      }

      return this.convenienceRewardsClaimed.length;
   }

   public int convenienceRewardMilestone(int var1) {
      return var1 >= 0 && var1 < CONVENIENCE_REWARD_MILESTONES.length ? CONVENIENCE_REWARD_MILESTONES[var1] : -1;
   }

   public boolean convenienceRewardReady(int var1) {
      return var1 >= 0 && var1 < this.convenienceRewardsClaimed.length && !this.convenienceRewardsClaimed[var1] && this.convenienceRewardCollectedCount(var1) >= CONVENIENCE_REWARD_MILESTONES[var1];
   }

   public int convenienceRewardCollectedCount(int var1) {
      return var1 == CONVENIENCE_REWARD_MILESTONES.length - 1 ? this.lietHoaCollectedSpeciesCount() : this.collectedSpeciesCount();
   }

   public boolean claimConvenienceReward(int var1) {
      if (!this.convenienceRewardReady(var1)) {
         return false;
      } else {
         this.convenienceRewardsClaimed[var1] = true;
         return true;
      }
   }

   public boolean[] convenienceRewardsSnapshot() {
      return Arrays.copyOf(this.convenienceRewardsClaimed, this.convenienceRewardsClaimed.length);
   }

   public void restore(byte[] var1) {
      Arrays.fill(this.states, (byte)0);
      Arrays.fill(this.v4States, (byte)0);
      Arrays.fill(this.mutationStates, (byte)0);
      if (var1 != null) {
         for(int var2 = 0; var2 < this.states.length && var2 < var1.length; ++var2) {
            byte var3 = var1[var2];
            this.states[var2] = var3 >= 0 && var3 <= 2 ? var3 : 0;
         }

         for(int var4 = 0; var4 < this.v4States.length && 202 + var4 < var1.length; ++var4) {
            byte var6 = var1[202 + var4];
            this.v4States[var4] = var6 >= 0 && var6 <= 2 ? var6 : 0;
         }

         for(int var5 = 0; var5 < this.mutationStates.length && 274 + var5 < var1.length; ++var5) {
            byte var7 = var1[274 + var5];
            this.mutationStates[var5] = var7 >= 0 && var7 <= 2 ? var7 : 0;
         }

      }
   }

   public void restoreConvenienceRewards(boolean[] var1) {
      Arrays.fill(this.convenienceRewardsClaimed, false);
      if (var1 != null) {
         System.arraycopy(var1, 0, this.convenienceRewardsClaimed, 0, Math.min(var1.length, this.convenienceRewardsClaimed.length));
      }
   }

   private static boolean validSpecies(int var0) {
      return var0 >= 0 && var0 < 202;
   }

   private static boolean validV4Species(int var0) {
      return var0 >= 202 && var0 < 274;
   }

   private static int mutationIndex(int var0) {
      if (LietHoaMutationCatalog.isSpecies(var0)) {
         return 0;
      } else if (ThienLuanMutationCatalog.isSpecies(var0)) {
         return 1;
      } else {
         return HoaDiemHauVuongCatalog.isSpecies(var0) ? 2 : (ThienViemHoCatalog.isSpecies(var0) ? 3 : -1);
      }
   }
}
