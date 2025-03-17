export interface GroupChangeRequestDto {
    accountId: string;
    groupName: string | undefined;
    accountsEmails: string[];
    version: string;
}