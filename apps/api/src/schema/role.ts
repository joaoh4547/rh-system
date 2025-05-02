import { z } from "zod";

export const createRoleSchema = z.object({
	name: z.string().min(0,{message: 'Name is required'}),
});

export const roleSchema = z.object({
	id: z.string().cuid(),
	name: z.string(),
	slug: z.string(),
	active: z.boolean(),
});

export const roleListSchema = z.object({
	roles: z.array(roleSchema),
});

export type CreateRole = z.infer<typeof createRoleSchema>;

export type Role = z.infer<typeof roleSchema>;
