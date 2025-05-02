
import { ResourceAlreadyExistsError } from "@/domain/exceptions/resource-already-exists";
import type { RoleRepository } from "@/domain/repositories/role-repository";

import { slugify } from "@rh-system/utils/slugfy";

interface CreateRoleUseInputParams {
	name: string;
}

type CreateRoleUseCaseResult = {
	role: {
		name: string;
		active: boolean;
	};
};

export class CreateRoleUseCase {
	constructor(private readonly roleRepository: RoleRepository) {}

	async handle({
		name,
	}: CreateRoleUseInputParams): Promise<CreateRoleUseCaseResult> {
		const role = {
			name,
			slug: slugify(name),
			active: true,
		};

		const roleWithSameSlug = await this.roleRepository.findBySlug(role.slug)

		if(roleWithSameSlug){
			throw new ResourceAlreadyExistsError('Already exists role with same name')
		}


		await this.roleRepository.create(role);
		return {
			role,
		}

	}
}