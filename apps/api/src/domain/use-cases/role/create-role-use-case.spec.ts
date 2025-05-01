import {CreateRoleUseCase} from "@/domain/use-cases/role/create-role-use-case";
import {InMemoryRoleRepository} from "@/test/repositories/in-memory-role-repository";
import {beforeEach, expect} from "vitest";

let roleRepository: InMemoryRoleRepository;
let sut: CreateRoleUseCase;

describe("Role Use Case", () => {
	beforeEach(() => {
		roleRepository = new InMemoryRoleRepository();
		sut = new CreateRoleUseCase(roleRepository);
	});

	it("should be able to create a role", async () => {
		await sut.handle({ name: "Development" });

		expect(roleRepository.roles).length(1);
		expect(roleRepository.roles[0]).toEqual(
			expect.objectContaining({
				name: "Development",
			}),
		);
	});

	it("should be generated a slug of role based in a name", async () => {
		await sut.handle({ name: "Web Development" });
		expect(roleRepository.roles[0].slug).toEqual("web-development");
	});
});
