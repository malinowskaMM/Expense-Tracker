import * as React from 'react';
import { TableHead, Box, Table, TableBody, Snackbar, Alert, TableCell, TableContainer, TableFooter, TablePagination, TableRow, Paper, styled, Checkbox, Button } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { GroupUserResponseDto } from '../../types/GroupUserResponseDto';
import { GroupChangeHeadRequestDto } from '../../types/GroupChangeHeadRequestDto';

const CustomPaginationUserAdminGroupTable = ({ props }) => {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(2);
    const [rows, setRows] = React.useState<GroupUserResponseDto[]>([]);
    const { t, i18n } = useTranslation();
    const [editableMode, setEditableMode] = React.useState(false);
    const [selectedRows, setSelectedRows] = React.useState([]);
    const [openFailedAlertByOutdatedData, setOpenFailedAlertByOutdatedData] = React.useState(false);

    let config = {
        method: 'GET',
        url: API_URL + '/groups/' + props.groupId + '/users',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchData = async () => {
        axios.request(config)
            .then((response) => {
                setRows(response.data.users);
            }).catch((error) => {
            })

    }

    React.useEffect(() => {
        console.log(config.url);
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

    const handleEditAdmins = () => {
        setEditableMode(true);
    }

    const handleCancelEditAdmins = () => {
        setEditableMode(false);
    }

    const handleChangeFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenFailedAlertByOutdatedData(false);
    };

    const handleCheckboxChange = (row) => (event) => {
        const checked = event.target.checked;
        if (checked) {
            // Add the row to selectedRows if it's not already included
            if (!selectedRows.includes(row)) {
                setSelectedRows([...selectedRows, row]);
            }
        } else {
            // Remove the row from selectedRows
            setSelectedRows(selectedRows.filter((selectedRow) => selectedRow !== row));
        }
    };

    const handleSaveEditAdmins = () => {
        let request: GroupChangeHeadRequestDto = {
            newOwnerIds: selectedRows.map((row) => row.id),
            sign: props.sign,
            version: props.version
        }

        let config = {
            method: 'PATCH',
            url: API_URL + '/groups/group/' + props.groupId + '/head',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'If-Match': props.sign
            },
            data: request
        };

        const changeAdmins = async () => {
            axios.request(config)
                .then((response) => {

                    featchData();
                    setOpenFailedAlertByOutdatedData(false);

                }).catch((error) => {

                    if (error.response && error.response.status === 422) {
                        setOpenFailedAlertByOutdatedData(true);
                    }
                })

        }

        changeAdmins();
        setEditableMode(false);
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

    const StyledChangeAdminBox = styled(StyledButton)({
        backgroundColor: '#ED6C02',
        fontcolor: '#1B2024',
        width: '200px',
        height: '50px',
    })

    const StyledCancel = styled(StyledButton)({
        backgroundColor: '#ea4444',
        fontcolor: '#1B2024',
        width: '100px',
        height: '50px',
    })

    const StyledSave = styled(StyledButton)({
        backgroundColor: '#60a840',
        fontcolor: '#1B2024',
        width: '100px',
        height: '50px',
    })


    return (
        <div>
            <TableContainer component={Paper}>
                <Table aria-label="custom pagination table">
                    <TableHead>
                        <TableRow >
                            <TableCell>
                                <div className="font-link">
                                    {t('groupPanel.email')}
                                </div>
                            </TableCell>
                            <TableCell>
                                <div className="font-link" style={{ textAlign: 'left' }}>
                                    {t('groupPanel.role')}
                                </div>
                            </TableCell>
                            <TableCell>
                                <div className="font-link" style={{ textAlign: 'left' }}>
                                    {t('groupPanel.isAdmin')}
                                </div>
                            </TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {(rowsPerPage > 0
                            ? rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                            : rows
                        ).map((row) => (
                            <TableRow key={row.id}>
                                <TableCell component="th" scope="row">
                                    {row.email}
                                </TableCell>
                                <TableCell align="left">
                                    {row.roleInCurrentGroup === "ADMIN" ? (
                                        <StyledAdminBox>
                                            {t('groupPanel.admin')}
                                        </StyledAdminBox>
                                    ) : (
                                        <StyledRoleBox>
                                            {t('groupPanel.user')}
                                        </StyledRoleBox>
                                    )}
                                </TableCell>
                                <TableCell align="left">
                                    <Checkbox
                                        defaultChecked={row.roleInCurrentGroup === "ADMIN"}
                                        disabled={row.roleInCurrentGroup === "ADMIN" || !editableMode}
                                        onChange={handleCheckboxChange(row)}
                                    />

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

            <div style={{ paddingTop: '4%', paddingInline: '4%', display: 'flex', flexDirection: 'row' }}>
                <div style={{ marginRight: '2%' }}>
                    <StyledChangeAdminBox onClick={handleEditAdmins}>
                        {t('groupPanel.editAdministrators')}
                    </StyledChangeAdminBox>
                </div>
                {editableMode ? (
                    <>
                        <div style={{ marginRight: '2%' }}>
                            <StyledCancel onClick={handleCancelEditAdmins}>
                                {t('groupPanel.cancel')}
                            </StyledCancel>
                        </div>
                        <div>
                            <StyledSave onClick={handleSaveEditAdmins}>
                                {t('groupPanel.save')}
                            </StyledSave>
                        </div>
                    </>
                ) : null

                }
            </div>


            <Snackbar open={openFailedAlertByOutdatedData} autoHideDuration={6000} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleChangeFailedByOutdatedDataAlertClose}>
                    {t('common.optimisticLockError')}
                </Alert>
            </Snackbar>
        </div>
    );
}

export default CustomPaginationUserAdminGroupTable;