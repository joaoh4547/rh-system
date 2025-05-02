
import { CreateRoleUseCase } from "@/domain/use-cases/role/create-role-use-case";
import { PrismaRoleRepository } from "@/infra/database/prisma/repositories/prisma-role-repository";
import { createRoleSchema } from "@/schema/role";
import type { FastifyInstance } from "fastify";
import type { ZodTypeProvider } from "fastify-type-provider-zod";
import { z } from "zod";


export async function createRole(app: FastifyInstance) {
	app.withTypeProvider<ZodTypeProvider>().post(
		"/roles",
		{
			schema: {
				tags: ["roles"],
				summary: "Create new role",
				security: [{ bearerAuth: [] }],
				body: createRoleSchema,
				response: {
					201: z.null(),
					
				},
			},
		},
		async (request, reply) => {
			const { name } = request.body;

			const useCase = new CreateRoleUseCase(new PrismaRoleRepository());

			await useCase.handle({ name });

			return reply.status(201).send();
		},
	);
}
