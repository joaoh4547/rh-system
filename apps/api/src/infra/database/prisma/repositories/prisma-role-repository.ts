import type { RoleRepository } from "@/domain/repositories/role-repository";
import { prisma } from "@/lib/prisma";
import type { Role } from "@/schema/role";
import { slugify } from "@rh-system/utils/slugfy";

export class PrismaRoleRepository implements RoleRepository {
	async create(role: Role) {
		await prisma.role.create({
			data: {
				name: role.name,
				slug: slugify(role.name)
			},
		});
	}
}
