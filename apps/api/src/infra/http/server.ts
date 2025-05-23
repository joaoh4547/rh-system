import * as console from "node:console";
import { createRole } from "@/infra/http/routes/roles/create-role";
import fastifySwagger from "@fastify/swagger";
import fastifySwaggerUi from "@fastify/swagger-ui";
import { env } from "@rh-system/env";
import { fastify } from "fastify";
import {
	type ZodTypeProvider,
	jsonSchemaTransform,
	serializerCompiler,
	validatorCompiler,
} from "fastify-type-provider-zod";
import { errorHandler } from "./error-handler";

const app = fastify().withTypeProvider<ZodTypeProvider>();

app.setErrorHandler(errorHandler)
app.setValidatorCompiler(validatorCompiler);
app.setSerializerCompiler(serializerCompiler);

app.register(fastifySwagger, {
	openapi: {
		info: {
			title: "RH System API",
			description: "Web API for RH System",
			version: "1.0.0",
		},
		components: {
			securitySchemes: {
				bearerAuth: {
					type: "http",
					scheme: "bearer",
					bearerFormat: "JWT",
				},
			},
		},
	},
	transform: jsonSchemaTransform,
});

app.register(fastifySwaggerUi, {
	routePrefix: "/api-docs",
});

// Role Routes
app.register(createRole);


app.listen({ port: env.SERVER_PORT }).then(() => {
	console.log(`HTTP Running on http:localhost:${env.SERVER_PORT}`);
})
