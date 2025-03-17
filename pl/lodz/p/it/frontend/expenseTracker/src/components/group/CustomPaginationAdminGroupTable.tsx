
import * as React from 'react';
import { useTheme } from '@mui/material/styles';
import { KeyboardArrowRight, KeyboardArrowLeft } from '@mui/icons-material';
import FirstPageIcon from '@mui/icons-material/FirstPage';
import LastPageIcon from '@mui/icons-material/LastPage';
import { Fab, TableHead, Button, styled, IconButton, Box, Table, TableBody, TableCell, TableContainer, TableFooter, AlertTitle, TablePagination, Snackbar, Alert, TableRow, Paper, Dialog, DialogActions, DialogContent, DialogTitle, DialogContentText, List, ListItem, Tooltip, Switch, Stack, Typography, TextField, Select, MenuItem } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { useNavigate } from 'react-router-dom';
import { GroupEntityResponseDto } from '../../types/GroupEntityResponseDto';
import { GroupUserListResponseDto } from '../../types/GroupUserListResponseDto'
import { GroupChangeHeadRequestDto } from '../../types/GroupChangeHeadRequestDto';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faRotateRight } from '@fortawesome/free-solid-svg-icons';
import CustomPaginationUserAdminGroupTable from './CustomPaginationUserAdminGroupTable';
import GroupData from '../../types/GroupData';


interface TablePaginationActionsProps {
    count: number;
    page: number;
    rowsPerPage: number;
    onPageChange: (
        event: React.MouseEvent<HTMLButtonElement>,
        newPage: number,
    ) => void;
}

function TablePaginationActions(props: TablePaginationActionsProps) {
    const theme = useTheme();
    const { count, page, rowsPerPage, onPageChange } = props;
    const { t, i18n } = useTranslation();

    const handleFirstPageButtonClick = (
        event: React.MouseEvent<HTMLButtonElement>,
    ) => {
        onPageChange(event, 0);
    };

    const handleBackButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, page - 1);
    };

    const handleNextButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, page + 1);
    };

    const handleLastPageButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        onPageChange(event, Math.max(0, Math.ceil(count / rowsPerPage) - 1));
    };

    return (
        <Box sx={{ flexShrink: 0, ml: 2.5 }}>
            <IconButton
                onClick={handleFirstPageButtonClick}
                disabled={page === 0}
                aria-label={t('common.firstPage')}
            >
                {theme.direction === 'rtl' ? <LastPageIcon /> : <FirstPageIcon />}
            </IconButton>
            <IconButton
                onClick={handleBackButtonClick}
                disabled={page === 0}
                aria-label={t('common.previousPage')}
            >
                {theme.direction === 'rtl' ? <KeyboardArrowRight /> : <KeyboardArrowLeft />}
            </IconButton>
            <IconButton
                onClick={handleNextButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label={t('common.nextPage')}
            >
                {theme.direction === 'rtl' ? <KeyboardArrowLeft /> : <KeyboardArrowRight />}
            </IconButton>
            <IconButton
                onClick={handleLastPageButtonClick}
                disabled={page >= Math.ceil(count / rowsPerPage) - 1}
                aria-label={t('common.lastPage')}
            >
                {theme.direction === 'rtl' ? <FirstPageIcon /> : <LastPageIcon />}
            </IconButton>
        </Box>
    );
}



export default function CustomPaginationAdminGroupTable() {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);
    const navigate = useNavigate();
    const [rows, setRows] = React.useState<GroupEntityResponseDto[]>([]);
    const [usersOfSelectedGroup, setUsersOfSelectedGroup] = React.useState<GroupUserListResponseDto[]>([]);
    const [sign, setSign] = React.useState<string>("");
    const [version, setVersion] = React.useState<string>("");
    const { t, i18n } = useTranslation();
    const token = "Bearer " + localStorage.getItem("token");
    const [validationErrors, setValidationErrors] = React.useState<string[]>([]);
    const [alertHeight, setAlertHeight] = React.useState(0);
    const [openUserGroupList, setOpenUserGroupList] = React.useState(false);
    const [selectedGroupId, setSelectedGroupId] = React.useState("");
    const [selectedGroupVersion, setSelectedGroupVersion] = React.useState("");
    const [selectedGroupSign, setSelectedGroupSign] = React.useState("");
    const [data, setData] = React.useState<GroupData>({});

    let config = {
        method: 'GET',
        url: API_URL + '/groups',
        headers: {
            'Authorization': token
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
        featchData();
    }, []);

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


    const handleClickOpenUserGroupList = (groupId: string, version: string, sign: string) => {
        setSelectedGroupId(groupId);
        setSelectedGroupVersion(version);
        setSelectedGroupSign(sign)
        let data: GroupData = {
            groupId: groupId,
            version: version,
            sign: sign
        }

        console.log(data);

        setData(data);
        setOpenUserGroupList(true);
    }

    const handleCloseUserGroupList = () => {
        setOpenUserGroupList(false);
    }

    const handleClickOpenReload = () => {
        featchData();
    }

    const StyledButton = styled(Button)({
        backgroundColor: '#308FFF',
        color: 'white',
        borderRadius: '5vh',
        fontSize: '0.7rem',
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })

    const StyledBox = styled(Box)({
        display: 'inline-block',
        border: '1px solid white',
        padding: '3%',
        borderRadius: '5vh',
        color: "#1E1E1E",
        fontSize: '0.7rem',
        textAlign: 'center' as const,
        fontWeight: 'bold' as const,
        fontFamily: [
            "Inter",
            'sans-serif',
        ].join(','),
    })

    const StyledRoleBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
    })

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

    return (
        <div>
            <div className="right-side-60">
                <Fab color="primary" aria-label="add" onClick={handleClickOpenReload}>
                    <FontAwesomeIcon icon={faRotateRight} />
                </Fab>
            </div>
            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 400 }} aria-label="custom pagination table">
                    <TableHead>
                        <TableRow >
                            <TableCell>
                                <div className="font-link">
                                    {t('customPaginationAdminGroupTable.groupName')}
                                </div>
                            </TableCell>
                            <TableCell align="left">
                                <div className="font-link">
                                    {t('customPaginationAdminGroupTable.groupAdmin')}
                                </div>
                            </TableCell>
                            <TableCell>
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
                                    {row.users.users.map((user: any) => {
                                        if (user.roleInCurrentGroup === 'ADMIN') {
                                            return (
                                                <StyledRoleBox>{user.email} </StyledRoleBox>
                                            )
                                        }
                                        return null;
                                    }
                                    )
                                    }
                                </TableCell>
                                <TableCell align="right">
                                    <IconButton onClick={() => handleClickOpenUserGroupList(row.groupId, row.version, row.sign)}>
                                        <StyledButton>
                                            {t('customPaginationAdminGroupTable.listMembers')}
                                        </StyledButton>
                                    </IconButton>
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
            </TableContainer>


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
                        <CustomPaginationUserAdminGroupTable props={data} />
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseUserGroupList} style={dialoButtonTextStyle}>
                        {t('groupPanel.exit')}
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}
