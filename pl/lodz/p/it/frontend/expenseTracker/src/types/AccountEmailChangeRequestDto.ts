export interface AccountEmailChangeRequestDto {
    newEmail: string;
    repeatedNewEmail: string;
    version: string;
    callbackRoute: string;
}