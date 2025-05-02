import { randomUUID } from "node:crypto";
import type { RoleRepository } from "@/domain/repositories/role-repository";
import type { Role } from "@/schema/role";
import type { Prisma } from "generated/prisma";

export class InMemoryRoleRepository implements RoleRepository {
	roles: Role[] = [];

	async create({ active, name, slug }: Prisma.RoleCreateInput) {
		this.roles.push({
			id: randomUUID(),
			active: active || true,
			name,
			slug,
		});
	}

	async findBySlug(slug: string) {
		const role = this.roles.find((x) => x.slug === slug) || null;
		return role;
	}
}
