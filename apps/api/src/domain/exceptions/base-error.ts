export class BaseError extends Error{

    constructor(readonly code: number, message?:string){
        super(message|| 'Unexpected Error')
    }
}