import type { RoleRepository } from "@/domain/repositories/role-repository";
import { type Prisma, prisma } from "@/lib/prisma";

export class PrismaRoleRepository implements RoleRepository {
	async create({ active, name, slug }: Prisma.RoleCreateInput) {
		await prisma.role.create({
			data: {
				name,
				slug,
				active,
			},
		});
	}

	async findBySlug(slug: string) {
		const findRole =
			(await prisma.role.findUnique({
				where: {
					slug,
				},
			})) || null;

		return findRole;
	}
}
