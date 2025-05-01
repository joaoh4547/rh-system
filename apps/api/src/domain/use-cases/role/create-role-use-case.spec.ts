import { ResourceAlreadyExistsError } from "@/domain/exceptions/resource-already-exists";
import { CreateRoleUseCase } from "@/domain/use-cases/role/create-role-use-case";
import { InMemoryRoleRepository } from "@/test/repositories/in-memory-role-repository";
import { beforeEach, expect } from "vitest";

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

	it("should be able to create a role in status active", async () => {
		await sut.handle({ name: "Development" });
		expect(roleRepository.roles[0].active).toEqual(true);
	});


	it("should be generated a slug of role based in a name", async () => {
		await sut.handle({ name: "Web Development" });
		expect(roleRepository.roles[0].slug).toEqual("web-development");
	});

	it("should not be able create an role with same name", async () => {
		await sut.handle({ name: "Development" });
		await expect(async () =>{
			await sut.handle({ name: "Development" })
		}).rejects.toThrowError(ResourceAlreadyExistsError)
	});
});
