import { z } from "zod";

const createEmployeeSchema = z.object({
	name: z.string(),
	email: z.string(),
	phone: z.string(),
	birthDate: z.date(),
	rg: z.string(),
	cpf: z.string(),
	roleId: z.string().cuid(),
	gender: z.enum(["male", "female"]),
});