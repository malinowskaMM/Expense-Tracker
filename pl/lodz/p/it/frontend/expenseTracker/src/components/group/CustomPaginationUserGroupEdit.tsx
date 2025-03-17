import * as React from 'react';
import { TableHead, Box, Table, TableBody, TableCell, TableContainer, TableFooter, TablePagination, TableRow, Paper, styled } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { GroupUserResponseDto } from '../../types/GroupUserResponseDto';

const CustomPaginationUserGroupEdit = ({ props }) => {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(2);
    const [rows, setRows] = React.useState<GroupUserResponseDto[]>([]);
    const { t, i18n } = useTranslation();

    let config = {
        method: 'GET',
        url: API_URL + '/groups/' + props + '/users',
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

    return (
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
    );

}

export default CustomPaginationUserGroupEdit;