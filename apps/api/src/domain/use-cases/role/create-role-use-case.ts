import type { CreateRole, Role } from "@/schema/role";

interface CreateRoleUseInputParams {
	name: string;
}

interface CreateRoleUseCaseResult {
	role: Role;
}

export class CreateRoleUseCase {
	async handle({
		name,
	}: CreateRoleUseInputParams): Promise<CreateRoleUseCaseResult> {
		return {
			role: {
				active: true,
				name,
			},
		};
	}
}
