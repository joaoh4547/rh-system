/*
  Warnings:

  - A unique constraint covering the columns `[slug]` on the table `role` will be added. If there are existing duplicate values, this will fail.
  - Added the required column `slug` to the `role` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE "role" ADD COLUMN     "slug" TEXT NOT NULL;

-- CreateIndex
CREATE UNIQUE INDEX "role_slug_key" ON "role"("slug");
