
import type { Role } from "@/schema/role";

export interface RoleRepository {
	create(role: Role): Promise<void>;

	findBySlug(slug: string): Promise<Role | null>
}