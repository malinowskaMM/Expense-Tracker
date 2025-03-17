export interface AccountPasswordChangeRequestDto {
    lastPassword: string;
    newPassword: string;
    repeatedNewPassword: string;
    version: number;
}