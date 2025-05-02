import { BaseError } from "@/domain/exceptions/base-error";
import type { FastifyInstance } from "fastify";
import {
	type ZodFastifySchemaValidationError,
	hasZodFastifySchemaValidationErrors,
} from "fastify-type-provider-zod";
import { ZodError } from "zod";

type FastifyErrorHandler = FastifyInstance["errorHandler"];

type ZodErrorType = {
	[k: string]: string[];
};

export const errorHandler: FastifyErrorHandler = (error, req, reply) => {
	if (error instanceof ZodError) {
		return reply.status(400).send({
			message: "Validation error",
			errors: error.flatten().fieldErrors,
		});
	}

	if (hasZodFastifySchemaValidationErrors(error)) {
		const validation = error.validation as ZodFastifySchemaValidationError[];

		const errors = validation.reduce((acc, val) => {
			const fieldName = val.params.issue.path.join(".");
			if (!acc[fieldName]) {
				acc[fieldName] = [];
			}
			acc[fieldName].push(val.params.issue.message);
			return acc;
		}, {} as ZodErrorType);

		return reply.status(400).send({
			message: "Validation error",
			errors,
		});
	}

    if(error instanceof BaseError){
        return reply.status(error.statusCode).send({
			message: error.message
		});
    }

	return reply.status(500).send({ message: "Internal Server Error" });
};
