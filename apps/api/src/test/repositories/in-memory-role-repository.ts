import type { RoleRepository } from "@/domain/repositories/role-repository";
import type { Role } from "@/schema/role";

export class InMemoryRoleRepository implements RoleRepository {
	roles: Role[] = [];

	async create(role: Role) {
		this.roles.push(role);
	}

	async findBySlug(slug: string) {
		const role = this.roles.find(x => x.slug === slug) || null
		return role
	}
}
