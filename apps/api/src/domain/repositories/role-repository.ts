
import type { CreateRole, Role } from "@/schema/role";

export interface RoleRepository {
	create(role: CreateRole): Promise<void>;

	findBySlug(slug: string): Promise<Role | null>
}