import { createEnv } from '@t3-oss/env-nextjs';
import { z } from 'zod';


export const env = createEnv({
    server: {
        SERVER_PORT: z.coerce.number().default(3333),
        DATABASE_URL: z.string().url(),
      },
      client: {},
      shared: {},
      runtimeEnv: {
        DATABASE_URL: process.env.DATABASE_URL,
        SERVER_PORT: process.env.SERVER_PORT,
      },
      emptyStringAsUndefined: true
  });