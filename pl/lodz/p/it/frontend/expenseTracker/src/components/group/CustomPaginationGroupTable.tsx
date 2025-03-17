import * as React from 'react';
import { TableHead, Button, styled, Box, Table, TableBody, TableCell, TableContainer, TableFooter, TablePagination, TableRow, Paper, IconButton, Dialog, DialogActions, DialogContent, DialogTitle, DialogContentText, Snackbar, Alert, ListItem, TextField, Grid, List, ListItemIcon, Checkbox, ListItemText } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { useNavigate } from 'react-router-dom';
import { GroupResponseDto } from '../../types/GroupResponseDto';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPen, faArrowRightFromBracket } from '@fortawesome/free-solid-svg-icons';
import CustomPaginationUserGroupTable from './CustomPaginationUserGroupTable';
import { GroupChangeRequestDto } from '../../types/GroupChangeRequestDto';
import { useForm } from "react-hook-form";
import { GroupUserResponseDto } from '../../types/GroupUserResponseDto';
import { SubmitHandler } from 'react-hook-form';

function intersection(a: readonly GroupUserResponseDto[], b: readonly GroupUserResponseDto[]) {
    return a.filter((value) => b.indexOf(value) !== -1);
}

function not(a: readonly GroupUserResponseDto[], b: readonly GroupUserResponseDto[]) {
    return a.filter((value) => b.indexOf(value) === -1);
}

export default function CustomPaginationGroupTable({ updateTable }) {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(2);
    const navigate = useNavigate();
    const [rows, setRows] = React.useState<GroupResponseDto[]>([]);
    const { t, i18n } = useTranslation();
    const [openLeaveGroupDialog, setOpenLeaveGroupDialog] = React.useState(false);
    const [selectedRowId, setSelectedRowId] = React.useState<number>();
    const [openUserGroupList, setOpenUserGroupList] = React.useState(false);
    const [selectedGroupId, setSelectedGroupId] = React.useState<number>();
    const [openUserGroupEdit, setOpenUserGroupEdit] = React.useState(false);
    const [nameState, setNameState] = React.useState<string>();
    const [openGroupAddFailedAlert, setOpenGroupAddFailedAlert] = React.useState(false);
    const [openGroupEditFailedAlert, setOpenGroupEditFailedAlert] = React.useState(false);
    const [groupSign, setGroupSign] = React.useState<string>('');
    const [groupVersion, setGroupVersion] = React.useState<string>('');
    const [openFailedAlertByOutdatedData, setOpenFailedAlertByOutdatedData] = React.useState(false);

    const [checked, setChecked] = React.useState<readonly GroupUserResponseDto[]>([]);
    const [leftUsersOutGroup, setLeftUsersOutGroup] = React.useState<GroupUserResponseDto[]>([]); //left
    const [rightUsersInGroup, setRightUsersInGroup] = React.useState<GroupUserResponseDto[]>([]); //right

    const leftChecked = intersection(checked, leftUsersOutGroup);
    const rightChecked = intersection(checked, rightUsersInGroup);


    const handleToggle = (value: GroupUserResponseDto) => () => {
        const currentIndex = checked.indexOf(value);
        const newChecked = [...checked];

        if (currentIndex === -1) {
            newChecked.push(value);
        } else {
            newChecked.splice(currentIndex, 1);
        }

        setChecked(newChecked);
    };

    const handleAllRight = () => {
        setRightUsersInGroup(rightUsersInGroup.concat(leftUsersOutGroup));
        setLeftUsersOutGroup([]);
    };

    const handleCheckedRight = () => {
        setRightUsersInGroup(rightUsersInGroup.concat(leftChecked));
        setLeftUsersOutGroup(not(leftUsersOutGroup, leftChecked));
        setChecked(not(checked, leftChecked));
    };

    const handleCheckedLeft = () => {
        setLeftUsersOutGroup(leftUsersOutGroup.concat(rightChecked));
        setRightUsersInGroup(not(rightUsersInGroup, rightChecked));
        setChecked(not(checked, rightChecked));
    };

    const handleAllLeft = () => {
        setLeftUsersOutGroup(leftUsersOutGroup.concat(rightUsersInGroup));
        setRightUsersInGroup([]);
    };

    const handleGroupNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setNameState(e.target.value);
    }

    const onSubmit = (data: GroupChangeRequestDto) => {
        let config = {
            method: 'PATCH',
            url: API_URL + '/groups/group/' + selectedRowId,
            headers: {
                'Content-Type': 'application/json',
                'If-Match': groupSign,
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: data

        };

        const editGroup = async () => {
            axios.request(config)
                .then((response) => {
                    setNameState(' ');
                    setRightUsersInGroup([]);

                    setOpenUserGroupEdit(false);
                    setOpenFailedAlertByOutdatedData(false);
                    featchData();
                }).catch((error) => {
                    if (error.response && error.response.status === 422) {
                        setOpenFailedAlertByOutdatedData(true);
                        setOpenGroupEditFailedAlert(false);
                    } else {
                        setOpenFailedAlertByOutdatedData(false);
                        setOpenGroupEditFailedAlert(true);
                    }
                })

        }
        editGroup();
    };

    const handleGroupAddFailedAlertClose = () => {
        setOpenGroupAddFailedAlert(false);
    }

    const handleGroupEditFailedAlertClose = () => {
        setOpenGroupEditFailedAlert(false);
    }

    const handleClickOpenUserGroupList = (groupId: number) => {
        setSelectedGroupId(groupId);
        setOpenUserGroupList(true);
    }

    const handleCloseUserGroupList = () => {
        setOpenUserGroupList(false);
    }

    const handleChangeFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenFailedAlertByOutdatedData(false);
    };

    let config = {
        method: 'GET',
        url: API_URL + '/groups/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchData = async () => {
        axios.request(config)
            .then((response) => {
                setRows(response.data.groups);
            }).catch((error) => {
            })

    }

    React.useEffect(() => {
        if (updateTable) {
            featchData();
        }

    }, [updateTable]);


    // Avoid a layout jump when reaching the last page with empty rows.
    const emptyRows =
        page > 0 ? Math.max(0, (1 + page) * rowsPerPage - rows.length) : 0;

    const handleChangePage = (
        event: React.MouseEvent<HTMLButtonElement> | null,
        newPage: number,
    ) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (
        event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleEditGroup = (id: string) => {
        getAccounts(id);
        getGroupUsers(id);
    }

    const handleLeaveGroup = () => {
        let config = {
            method: 'PATCH',
            url: API_URL + '/groups/group/' + selectedRowId + '/leave',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: localStorage.getItem("id")

        };

        const leaveGroup = async () => {
            axios.request(config)
                .then((response) => {
                    setOpenLeaveGroupDialog(false);
                    featchData();
                    setOpenFailedAlertByOutdatedData(false);
                }).catch((error) => {
                    if (error.response && error.response.status === 422) {
                        setOpenFailedAlertByOutdatedData(true);
                    }
                })

        }
        leaveGroup();
    }

    const handleLeaveGroupDialog = (groupId: number) => {
        setSelectedRowId(groupId);
        setOpenLeaveGroupDialog(true);
    }

    const handleLeaveGrouptDialogClose = () => {
        setOpenLeaveGroupDialog(false);
    }

    const handleEditGroupDialog = (groupId: number) => {
        setSelectedRowId(groupId);

        handleEditGroup(groupId.toString());

        setOpenUserGroupEdit(true);
    }


    const handleUserGroupEdit = () => {

        let data: GroupChangeRequestDto = {
            accountId: localStorage.getItem("id") != undefined ? localStorage.getItem("id")?.toString() : ' ',
            groupName: nameState != undefined ? nameState : ' ',
            accountsEmails: rightUsersInGroup.map((user) => user.email),
            version: groupVersion
        }

        onSubmit(data);
    }

    const handleCloseUserGroupEdit = () => {
        setOpenUserGroupEdit(false);
    }

    const dialoButtonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 0.7)',
        borderRadius: '5vh',
        color: '#1B2024',
        padding: '1%',
        fontWeight: '100',
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
        width: '150px',
        height: '30px',
    }

    const StyledBox = styled(Box)({
        display: 'inline-block',
        border: '1px solid white',
        padding: '3%',
        borderRadius: '5vh',
        color: "#1E1E1E",
        fontSize: '0.7rem',
        textTransform: 'uppercase',
        textAlign: 'center' as const,
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        color: '#1B2024',
    };

    const StyledAdminBox = styled(StyledBox)({
        backgroundColor: '#ED6C02',
        fontcolor: '#1B2024',
        fontWeight: 'bolder',
    })

    const StyledRoleBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
        fontcolor: '#1B2024',
        fontWeight: 'bolder',
    })

    const StyledDisplayUserListBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#389BD2',
        color: 'white',
    })

    const StyledEditGroupBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#4CAF50',
        color: 'white',
        padding: '13%',
    })

    const StyledLeaveGroupBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#FF3030',
        color: 'white',
        padding: '13%',
    })

    const getAccounts = (id: string) => {
        const config = {
            method: 'GET',
            url: API_URL + `/groups/` + id + `/out/users`,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const getAccounts = async () => {
            axios.request(config)
                .then((response) => {
                    setLeftUsersOutGroup(response.data.users.filter((user: GroupUserResponseDto) => user.id != localStorage.getItem("id")));
                }).catch((error) => {
                })

        }

        getAccounts();
    };

    const getGroupUsers = (id: string) => {
        const config = {
            method: 'GET',
            url: API_URL + `/groups/group/` + id,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const getGroupUsers = async () => {
            axios.request(config)
                .then((response) => {
                    setNameState(response.data.groupName);
                    setRightUsersInGroup(response.data.users.users.filter((user: GroupUserResponseDto) => user.id.toString() != localStorage.getItem("id")));
                    setGroupSign(response.data.sign);
                    setGroupVersion(response.data.version);
                }).catch((error) => {
                })

        }

        getGroupUsers();

    };

    const customList = (items: readonly GroupUserResponseDto[]) => (
        <Paper sx={{ width: 200, height: 230, overflow: 'auto' }}>
            <List dense component="div" role="list">
                {items.map((account: GroupUserResponseDto) => {
                    const labelId = `transfer-list-item-${account.id}-label`;

                    return (
                        <ListItem
                            key={account.id}
                            role="listitem"
                            button
                            onClick={handleToggle(account)}
                        >
                            {account.roleInCurrentGroup !== "ADMIN" && (
                                <ListItemIcon>
                                    <Checkbox
                                        checked={checked.indexOf(account) !== -1}
                                        disabled={account.roleInCurrentGroup === "ADMIN"}
                                        tabIndex={-1}
                                        disableRipple
                                        inputProps={{
                                            'aria-labelledby': labelId,
                                        }}
                                    />
                                </ListItemIcon>
                            )}
                            <ListItemText id={labelId} primary={account.email} />
                        </ListItem>
                    );
                })}
            </List>
        </Paper>
    );

    return (
        <TableContainer component={Paper}>
            <Table aria-label="custom pagination table">
                <TableHead>
                    <TableRow >
                        <TableCell>
                            <div className="font-link">
                                {t('groupPanel.groupName')}
                            </div>
                        </TableCell>
                        <TableCell>
                            <div className="font-link" style={{ textAlign: 'left' }}>
                                {t('groupPanel.yourRole')}
                            </div>
                        </TableCell>
                        <TableCell>
                            <div className="font-link">
                                {t('groupPanel.options')}
                            </div>
                        </TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {(rowsPerPage > 0
                        ? rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        : rows
                    ).map((row) => (
                        <TableRow key={row.groupId}>
                            <TableCell component="th" scope="row">
                                {row.groupName}
                            </TableCell>
                            <TableCell align="left">
                                {row.accountRole === "ADMIN" ? (
                                    <StyledAdminBox>
                                        {t('groupPanel.admin')}
                                    </StyledAdminBox>
                                ) : (
                                    <StyledRoleBox>
                                        {t('groupPanel.user')}
                                    </StyledRoleBox>
                                )}
                            </TableCell>
                            <TableCell align="right">
                                <TableRow>
                                    <IconButton onClick={() => handleClickOpenUserGroupList(row.groupId)}>
                                        <StyledDisplayUserListBox>
                                            {t('groupPanel.userListDisplay')}
                                        </StyledDisplayUserListBox>
                                    </IconButton>
                                </TableRow>
                                <TableRow>
                                    {row.accountRole === "ADMIN" ? (
                                        <IconButton onClick={() => handleEditGroupDialog(row.groupId)}>
                                            <StyledEditGroupBox>
                                                <FontAwesomeIcon icon={faPen} />
                                                {t('groupPanel.edit')}
                                            </StyledEditGroupBox>
                                        </IconButton>
                                    ) : (
                                        <IconButton onClick={() => handleLeaveGroupDialog(row.groupId)}>
                                            <StyledLeaveGroupBox>
                                                <FontAwesomeIcon icon={faArrowRightFromBracket} />
                                                {t('groupPanel.leaveGroup')}
                                            </StyledLeaveGroupBox>
                                        </IconButton>
                                    )}
                                </TableRow>
                            </TableCell>
                        </TableRow>
                    ))}
                    {emptyRows > 0 && (
                        <TableRow style={{ height: 53 * emptyRows }}>
                            <TableCell colSpan={4} />
                        </TableRow>
                    )}
                </TableBody>
                <TableFooter>
                    <TableRow>
                        <TablePagination
                            rowsPerPageOptions={[1, 5, 10, 25, { label: t('common.all'), value: -1 }]}
                            colSpan={3}
                            count={rows.length}
                            rowsPerPage={rowsPerPage}
                            page={page}
                            labelRowsPerPage={t('common.rowsPerPage')}
                            SelectProps={{

                                native: true,
                            }}
                            onPageChange={handleChangePage}
                            onRowsPerPageChange={handleChangeRowsPerPage}
                        //ActionsComponent={TablePagination}
                        />
                    </TableRow>
                </TableFooter>
            </Table>

            <Dialog
                open={openLeaveGroupDialog}
                onClose={handleLeaveGroup}
                PaperProps={{
                    style: {
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle id="alert-dialog-title">
                    {t('groupPanel.leaveGroupTitle')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('groupPanel.leaveGroupDescription')}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleLeaveGrouptDialogClose} style={buttonTextStyle}>
                        {t('groupPanel.cancel')}
                    </Button>
                    <Button onClick={() => handleLeaveGroup()} style={buttonTextStyle} autoFocus>
                        {t('groupPanel.leaveGroup')}
                    </Button>
                </DialogActions>
            </Dialog>


            <Dialog
                open={openUserGroupList}
                onClose={handleCloseUserGroupList}
                PaperProps={{
                    style: {
                        minWidth: '50%',
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle id="alert-dialog-title">
                    {t('groupPanel.userGroupList')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('groupPanel.userGroupListDescription')}
                    </DialogContentText>
                    <div style={{ marginTop: '4%' }}>
                        <CustomPaginationUserGroupTable props={selectedGroupId} />
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseUserGroupList} style={dialoButtonTextStyle}>
                        {t('groupPanel.exit')}
                    </Button>
                </DialogActions>
            </Dialog>


            <Dialog
                open={openUserGroupEdit}
                onClose={handleCloseUserGroupEdit}
                PaperProps={{
                    style: {
                        minWidth: '50%',
                        borderRadius: '40px',
                        padding: '2%',
                        backgroundColor: '#bef4da',
                        boxShadow: 'initial'
                    },
                }}
            >
                <DialogTitle id="alert-dialog-title">
                    {t('groupPanel.editGroup')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('groupPanel.editGroupDescription')}
                    </DialogContentText>
                    <div style={{ marginTop: '4%' }}>
                        <Box component="form">
                            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                <ListItem>
                                    <div className="font-link">
                                        {t('groupPanel.groupName')}*
                                    </div>
                                </ListItem>
                                <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                    <TextField
                                        value={nameState}
                                        required
                                        id="name"
                                        style={{ backgroundColor: 'white', width: '100%' }}
                                        variant='standard'
                                        onChange={handleGroupNameChange}
                                    />
                                </ListItem>
                                <ListItem>
                                    <div className="font-link">
                                        {t('groupPanel.groupUsers')}*
                                    </div>
                                </ListItem>
                                <ListItem>
                                    <Grid container spacing={2} justifyContent="center" alignItems="center">
                                        <Grid item>
                                            <ListItem>
                                                <div className="font-link">
                                                    {t('groupPanel.allUsers')}
                                                </div>
                                            </ListItem>
                                            {customList(leftUsersOutGroup)}
                                        </Grid>
                                        <Grid item>
                                            <Grid container direction="column" alignItems="center">
                                                <Button
                                                    sx={{ my: 0.5 }}
                                                    variant="outlined"
                                                    size="small"
                                                    onClick={handleAllRight}
                                                    disabled={leftUsersOutGroup.length === 0}
                                                    aria-label="move all right"
                                                >
                                                    ≫
                                                </Button>
                                                <Button
                                                    sx={{ my: 0.5 }}
                                                    variant="outlined"
                                                    size="small"
                                                    onClick={handleCheckedRight}
                                                    disabled={leftChecked.length === 0}
                                                    aria-label="move selected right"
                                                >
                                                    &gt;
                                                </Button>
                                                <Button
                                                    sx={{ my: 0.5 }}
                                                    variant="outlined"
                                                    size="small"
                                                    onClick={handleCheckedLeft}
                                                    disabled={rightChecked.length === 0}
                                                    aria-label="move selected left"
                                                >
                                                    &lt;
                                                </Button>
                                                <Button
                                                    sx={{ my: 0.5 }}
                                                    variant="outlined"
                                                    size="small"
                                                    onClick={handleAllLeft}
                                                    disabled={rightUsersInGroup.length === 0}
                                                    aria-label="move all left"
                                                >
                                                    ≪
                                                </Button>
                                            </Grid>
                                        </Grid>
                                        <Grid item>
                                            <ListItem>
                                                <div className="font-link">
                                                    {t('groupPanel.currentGroupUsers')}
                                                </div>
                                            </ListItem>
                                            {customList(rightUsersInGroup)}

                                        </Grid>
                                    </Grid>
                                </ListItem>
                            </div>
                        </Box>
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseUserGroupEdit} style={dialoButtonTextStyle}>
                        {t('groupPanel.exit')}
                    </Button>
                    <Button onClick={handleUserGroupEdit} style={dialoButtonTextStyle}>
                        {t('groupPanel.edit')}
                    </Button>
                </DialogActions>
            </Dialog>


            <Snackbar open={openGroupAddFailedAlert} autoHideDuration={6000} onClose={handleGroupAddFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleGroupAddFailedAlertClose}>
                    {t('groupPanel.addGroupFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openGroupEditFailedAlert} autoHideDuration={6000} onClose={handleGroupEditFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleGroupEditFailedAlertClose}>
                    {t('groupPanel.editGroupFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openFailedAlertByOutdatedData} autoHideDuration={6000} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                    {t('common.optimisticLockError')}
                </Alert>
            </Snackbar>
        </TableContainer>
    );
}

