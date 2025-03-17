import { GroupUserListResponseDto } from './GroupUserListResponseDto';

export interface GroupEntityResponseDto {
    groupId: string;
    groupName: string;
    sign: string;
    version: string;
    users: GroupUserListResponseDto[];
}