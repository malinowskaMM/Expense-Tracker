import * as React from 'react';
import { TableHead, Box, Table, TableBody, TableCell, TableContainer, TableFooter, TablePagination, TableRow, Paper, IconButton, styled, Button, Dialog, DialogTitle, DialogActions, DialogContent, DialogContentText, Snackbar, Alert, ListItem, TextField } from '@mui/material';
import { useTranslation } from "react-i18next";
import axios from 'axios';
import { API_URL } from '../../consts';
import { useNavigate } from 'react-router-dom';
import { CategoryResponseDto } from '../../types/CategoryResponseDto';
import { CategoryChangeRequestDto } from '../../types/CategoryChangeRequestDto';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPen, faTrash } from '@fortawesome/free-solid-svg-icons';
import { set, useForm } from 'react-hook-form';
import { ChromePicker, ColorResult } from 'react-color';
import reactCSS from 'reactcss';
import { useState } from 'react';

export default function CustomPaginationCategoryTable() {
    const [page, setPage] = React.useState(0);
    const [rowsPerPage, setRowsPerPage] = React.useState(5);
    const navigate = useNavigate();
    const [rows, setRows] = React.useState<CategoryResponseDto[]>([]);
    const { t, i18n } = useTranslation();
    const [openCategoryDeleteDialog, setOpenCategoryDeleteDialog] = React.useState(false);
    const [selectedRowId, setSelectedRowId] = React.useState('');
    const [openCategoryAddFailedAlert, setOpenCategoryAddFailedAlert] = React.useState(false);
    const [openCategoryEditDialog, setOpenCategoryEditDialog] = React.useState(false);
    const [openCategoryDeleteFailedAlert, setOpenCategoryDeleteFailedAlert] = React.useState(false);
    const { register, handleSubmit } = useForm<CategoryChangeRequestDto>();
    const [displayColorPicker, setDisplayColorPicker] = useState(false);
    const [colorState, setColorState] = useState("#6AE09E");
    const [nameState, setNameState] = useState("");
    const [descriptionState, setDescriptionState] = useState("");
    const [openCategoryEditFailedAlert, setOpenCategoryEditFailedAlert] = React.useState(false);
    const [categorySign, setCategorySign] = React.useState("");
    const [categoryVersion, setCategoryVersion] = React.useState("");
    const [openFailedAlertByOutdatedData, setOpenFailedAlertByOutdatedData] = React.useState(false);

    const config = {
        method: 'GET',
        url: API_URL + '/groups/all/categories/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchTableData = async () => {
        axios.request(config)
            .then((response) => {
                setRows(response.data.categories);
            })

    }

    React.useEffect(() => {
        featchTableData();

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

    const handleOpenCategoryDeleteDialog = (categoryId: string) => {
        setOpenCategoryDeleteDialog(true);
        setSelectedRowId(categoryId);
    }

    const handleCategoryEditFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenCategoryEditFailedAlert(false);
    }

    const handleCategoryDeleteFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenCategoryDeleteFailedAlert(false);
    }

    const handleChangeFailedByOutdatedDataAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') { return; }
        setOpenFailedAlertByOutdatedData(false);
    };

    const handleCategoryDelete = () => {

        const config = {
            method: 'DELETE',
            url: API_URL + '/categories/category/' + selectedRowId + '/by/' + localStorage.getItem("id"),
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const featchData = async () => {
            axios.request(config)
                .then((response) => {
                    setOpenCategoryDeleteDialog(false);
                    featchTableData();
                }).catch((error) => {
                    setOpenCategoryDeleteDialog(false);
                    setOpenCategoryDeleteFailedAlert(true);
                })

        }
        featchData();
    }

    const handleCategoryAddFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenCategoryAddFailedAlert(false);
    }

    const handleCloseCategoryDeleteDialog = () => {
        setOpenCategoryDeleteDialog(false);
    }

    const handleCategoryEdit = () => {
        setOpenCategoryEditDialog(false);
        onSubmit();
    }

    const handleCloseCategoryEditDialog = () => {
        setOpenCategoryEditDialog(false);
    }

    const handleCategoryEditDialog = (categoryId: string) => {
        setOpenCategoryEditDialog(true);
        setSelectedRowId(categoryId);

        let config = {
            method: 'GET',
            url: API_URL + '/categories/category/' + categoryId,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
        };

        const featchCategoryData = async () => {
            axios.request(config)
                .then((response) => {
                    setNameState(response.data.name);
                    setDescriptionState(response.data.description);
                    setColorState(response.data.color);
                    setCategorySign(response.data.sign);
                    setCategoryVersion(response.data.version);
                })
        }

        featchCategoryData();
    }

    const onSubmit = handleSubmit((data: CategoryChangeRequestDto) => {
        data.accountId = localStorage.getItem("id") ? localStorage.getItem("id").toString() : "";
        data.color = colorState;
        data.description = descriptionState;
        data.name = nameState;
        data.version = categoryVersion;

        const config = {
            method: 'PATCH',
            url: API_URL + '/categories/category/' + selectedRowId,
            headers: {
                'Content-Type': 'application/json',
                'If-Match': categorySign,
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: JSON.stringify(data),
        };


        axios.request(config)
            .then((response) => {
                setColorState("#6AE09E");
                setNameState("");
                setDescriptionState("");

                setOpenCategoryEditDialog(false);
                setOpenFailedAlertByOutdatedData(false);
                featchTableData();
            })
            .catch((error) => {
                if (error.response && error.response.status === 422) {
                    setOpenFailedAlertByOutdatedData(true);
                    setOpenCategoryEditDialog(false);
                } else {
                setOpenCategoryEditDialog(false);
                setOpenCategoryEditFailedAlert(true);
                }
            });

    });

    const handleCategoryNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setNameState(event.target.value);
    }

    const handleCategoryDescriptionChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        setDescriptionState(event.target.value);
    }

    const handleColorPicker = () => {
        setDisplayColorPicker(!displayColorPicker);
    };

    const handleClose = () => {
        setDisplayColorPicker(false);
    };

    const handleChange = (color: ColorResult) => {
        setColorState(color.hex);
        register("color");
    };


    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        color: '#1B2024',
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


    const StyledGroupBox = styled(StyledBox)({
        backgroundColor: '#E2E5E3',
        color: '#1E1E1E',
        fontWeight: 'bold',
        borderColor: '#E2E5E3',
    })

    const StyledEditCategoryBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#4CAF50',
        color: 'white',
        padding: '13%',
        borderColor: '#4CAF50'
    })

    const StyledDeleteCategoryBox = styled(StyledBox)({
        display: 'flex',
        backgroundColor: '#6F6F6F',
        color: 'white',
        padding: '13%',
        borderColor: '#6F6F6F'
    })

    const styles = reactCSS({
        'default': {
            color: {
                width: '100%',
                height: '50px',
                borderRadius: '2px',
                background: `${colorState}`,
            },
            swatch: {
                cursor: 'pointer',
                width: '100%'
            }
        },
    });

    return (
        <TableContainer component={Paper}>
            <Table aria-label="custom pagination table">
                <TableHead>
                    <TableRow >
                        <TableCell>
                            <div className="font-link-table-header">
                                {t('categoryPanel.categoryName')}
                            </div>
                        </TableCell>
                        <TableCell>
                            <div className="font-link-table-header" style={{ textAlign: 'left' }}>
                                {t('categoryPanel.group')}
                            </div>
                        </TableCell>
                        <TableCell>
                            <div className="font-link-table-header">
                                {t('categoryPanel.options')}
                            </div>
                        </TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {(rowsPerPage > 0
                        ? rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
                        : rows
                    ).map((row) => (
                        <TableRow key={row.id} style={{ backgroundColor: row.color }}>
                            <TableCell component="th" scope="row">
                                {row.name === "Default" ? (
                                    <>
                                        {t('common.default')}
                                    </>
                                ) :
                                row.name
                                }
                            </TableCell>
                            <TableCell align="left">
                                <StyledGroupBox>
                                    {row.groupName}
                                </StyledGroupBox>
                            </TableCell>
                            <TableCell align="left">
                                <IconButton onClick={() => handleOpenCategoryDeleteDialog(row.id)}>
                                    <StyledDeleteCategoryBox>
                                        <FontAwesomeIcon icon={faTrash} />
                                        {t('categoryPanel.delete')}
                                    </StyledDeleteCategoryBox>
                                </IconButton>
                                <IconButton onClick={() => handleCategoryEditDialog(row.id)}>
                                    <StyledEditCategoryBox>
                                        <FontAwesomeIcon icon={faPen} />
                                        {t('categoryPanel.edit')}
                                    </StyledEditCategoryBox>
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


            <Dialog
                open={openCategoryDeleteDialog}
                onClose={handleCloseCategoryDeleteDialog}
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
                    {t('categoryPanel.deleteCategory')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('categoryPanel.deleteCategoryDescription')}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseCategoryDeleteDialog} style={buttonTextStyle}>
                        {t('categoryPanel.cancel')}
                    </Button>
                    <Button onClick={() => handleCategoryDelete()} style={buttonTextStyle} autoFocus>
                        {t('categoryPanel.deleteCategoryButton')}
                    </Button>
                </DialogActions>
            </Dialog>


            <Dialog
                open={openCategoryEditDialog}
                onClose={handleCloseCategoryEditDialog}
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
                    {t('categoryPanel.editCategory')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('categoryPanel.editCategoryDescription')}
                    </DialogContentText>
                    <Box component="form" onSubmit={onSubmit}>
                        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <ListItem>
                                <div className="font-link">
                                    {t('categoryPanel.categoryName')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    {...register("name")}
                                    value={nameState}
                                    required
                                    id="name"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                    onChange={handleCategoryNameChange}
                                />
                            </ListItem>

                            <ListItem>
                                <div className="font-link">
                                    {t('categoryPanel.categoryColor')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <div style={styles.swatch} onClick={handleColorPicker}>
                                    <div style={styles.color} />
                                </div>
                                {displayColorPicker ? <div style={{ position: 'absolute', zIndex: 2 }}>
                                    <div onClick={handleClose} />
                                    <ChromePicker color={colorState} onChange={handleChange} />
                                </div> : null}
                            </ListItem>

                            <ListItem>
                                <div className="font-link">
                                    {t('categoryPanel.categoryDescription')}*
                                </div>
                            </ListItem>
                            <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                <TextField
                                    {...register("description")}
                                    value={descriptionState}
                                    required
                                    id="name"
                                    style={{ backgroundColor: 'white', width: '100%' }}
                                    variant='standard'
                                    onChange={handleCategoryDescriptionChange}
                                />
                            </ListItem>
                        </div>
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseCategoryEditDialog} style={buttonTextStyle}>
                        {t('categoryPanel.cancel')}
                    </Button>
                    <Button onClick={() => handleCategoryEdit()} style={buttonTextStyle} autoFocus>
                        {t('categoryPanel.edit')}
                    </Button>
                </DialogActions>
            </Dialog>

            <Snackbar open={openCategoryAddFailedAlert} autoHideDuration={6000} onClose={handleCategoryAddFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleCategoryAddFailedAlertClose}>
                    {t('categoryPanel.addCategoryFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openCategoryDeleteFailedAlert} autoHideDuration={6000} onClose={handleCategoryDeleteFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleCategoryDeleteFailedAlertClose}>
                    {t('categoryPanel.deleteCategoryFailed')}
                </Alert>
            </Snackbar>

            <Snackbar open={openCategoryEditFailedAlert} autoHideDuration={6000} onClose={handleCategoryEditFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleCategoryEditFailedAlertClose}>
                    {t('categoryPanel.editCategoryFailed')}
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
