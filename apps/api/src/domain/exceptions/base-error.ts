export class BaseError extends Error{

    readonly statusCode: number
    
    constructor( code?: number, message?:string){
        super(message|| 'Unexpected Error')
        this.statusCode = code || 500
    }
}