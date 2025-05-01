import { BaseError } from "./base-error";

export class ResourceAlreadyExistsError extends BaseError{
    constructor(message?: string){
        super(400,message || 'This resource already exists')
    }
}