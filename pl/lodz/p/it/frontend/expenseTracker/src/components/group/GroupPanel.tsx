import { Box } from '@mui/system';
import "./GroupPanel.css"
import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUsers } from '@fortawesome/free-solid-svg-icons';
import { TableHead, Paper, TableRow, TableCell, Table, Stack, Alert, AlertTitle, Snackbar, TableContainer, TableBody, ListItemButton, ListItemText, List, ListItem, TextField, Dialog, DialogActions, DialogTitle, DialogContent, Checkbox, Button, DialogContentText } from '@mui/material';
import CustomPaginationGroupTable from './CustomPaginationGroupTable';
import { useState } from 'react';
import axios from "axios";
import { AccountResponseDto } from '../../types/AccountResponseDto';
import { useEffect } from 'react';
import { API_URL } from '../../consts';
import { GroupCreateRequestDto } from '../../types/GroupCreateRequestDto';
import { faTrash } from '@fortawesome/free-solid-svg-icons';
import { useCallback } from 'react';

const GroupPanel = ( { refreshTable, featchGroupData } ) => {
    const { t, i18n } = useTranslation();
    const [openUserList, setOpenUserList] = useState(false);
    const [checked, setChecked] = useState([1]);
    const [rows, setRows] = useState<AccountResponseDto[]>([]);
    const [filter, setFilter] = useState('');
    const [filteredRows, setFilteredRows] = useState(rows);
    const [selectedUsers, setSelectedUsers] = useState<AccountResponseDto[]>([]);
    const [groupName, setGroupName] = useState('');
    const [userList, setUserList] = useState<AccountResponseDto[]>([]); 
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);
    const [openAddGroupSuccessAlert, setOpenAddGroupSuccessAlert] = useState(false);
    
    const handleToggle = (id: number) => () => {
        const currentIndex = checked.indexOf(id);
        const newChecked = [...checked];

        if (currentIndex === -1) {
            newChecked.push(id);
            const selectedUser = rows.find((user) => user.id === id);
            if (selectedUser) {
                setSelectedUsers((prevSelectedUsers) => [...prevSelectedUsers, selectedUser]);
            }
        } else {
            newChecked.splice(currentIndex, 1);
            setSelectedUsers((prevSelectedUsers) =>
                prevSelectedUsers.filter((user) => user.id !== id)
            );
        }

        setChecked(newChecked);
    };


    let config = {
        method: 'GET',
        url: API_URL + '/accounts/users',
        headers: {
            'Content-Type': 'application/json',
             'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchData = async () => {
        axios.request(config)
            .then((response) => {
                setRows(response.data.accounts);
                setFilteredRows(response.data.accounts.filter((item) =>item.id !== parseInt(localStorage.getItem("id")!)));
            }).catch((error) => {
            })

    }

    useEffect(() => {
        featchData();
    }, []);

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '50vh',
        width: '50vw',
    };

    const boxStyle = {
        marginTop: '35vh',
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '80%',
        minHeight: '600px',
        height: '100%',
        borderRadius: '5vh',
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '4.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
        paddingTop: '5vh',
    };

    const miniHeaderTextStyle = {
        textAlign: 'center' as const,
        fontSize: '2.0rem',
        color: 'rgba(43, 42, 42, 0.9)',
        paddingTop: '3vh',
    };

    const iconStyle = {
        color: '#99EDC5',
        fontSize: 100,
        paddingRight: '1vw',
    };

    const listStyle = {
        width: '80%',
        aliginItems: 'center',
        justifyContent: 'center'
    };

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

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

    const tableStyle = {
        backgroundColor: 'rgba(131, 225, 180, 1)',
    };

    const tableHeaderStyle = {
        backgroundColor: 'rgba(131, 225, 180, 1)',
        fontSize: '1.0rem',
        color: 'rgba(255, 255, 255, 1)',
        textAlign: 'center' as const,
        fontWeight: 'thin' as const,
    };

    const handleClickOpenUserList = () => {
        setOpenUserList(true);
    };

    const handleCloseUserList = () => {
        setOpenUserList(false);
    };

    const handleFilterChange = (event: any) => {
        const { value } = event.target;
        setFilter(value);

        const filteredData = rows.filter((item) =>
            item.email.toLowerCase().includes(value.toLowerCase())
        );
        setFilteredRows(filteredData);
    };

    const handleGroupNameChange = (event: any) => {
        const { value } = event.target;
        setGroupName(value);
        validateGroupName(event.target.value);
    };

    const validateGroupName = (name: string) => {
        const isValid = name.length > 2;
        updateValidationErrors('groupName', isValid);
        return isValid;
    };

    const validateSelectedUsers = () => {
        const isValid = selectedUsers.length > 0;
        updateValidationErrors('selectedUsers', isValid);
        return isValid;
    }

    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'groupName':
                        updatedErrors[field] = t('groupPanel.groupNameValidation');
                        break;
                    case 'selectedUsers':
                        updatedErrors[field] = t('groupPanel.selectedUsersValidation');
                        break;
                    default:
                        break;
                }
            } else {
                delete updatedErrors[field];
            }
            return updatedErrors;
        });
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
    };

    const handleClickAddUsersGroup = () => {
        // Filter users based on checked status and add them to the list
    const selectedUsers = filteredRows.filter((item) => checked.indexOf(item.id) !== -1);
    setUserList([...userList, ...selectedUsers]);

    // Reset the checked state
    setChecked([]);

    // Close the dialog or perform any other actions
    handleCloseUserList();
        setOpenUserList(false);
    }

    const updateTable = () => {
        featchData();
        refreshTable();
    }

    const updateTableCallback = useCallback(updateTable, [updateTable])

    const handleClickAddGroup = () => {
        if (!validateGroupName(groupName) || !validateSelectedUsers()) {
            return;
        }

        let newGroup : GroupCreateRequestDto = {
            name: groupName,
            accountsIds: userList.map((user) => user.id.toString()),
            ownerId: localStorage.getItem("id") !== undefined ? localStorage.getItem("id") : " "
        }

        let config = {
            method: 'POST',
            url: API_URL + '/groups/group',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: JSON.stringify(newGroup)

        };
            axios.request(config)
                .then((response) => {
                    setGroupName('');
                    setUserList([]);

                    setOpenUserList(false);
                    featchGroupData();
                    setOpenAddGroupSuccessAlert(true);
                }).catch((error) => {
                    setOpenAddGroupSuccessAlert(false);
                    featchData();
                    setGroupName('');
                    setSelectedUsers([]);
                })

    };

    const handleAddGroupSuccessAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenAddGroupSuccessAlert(false);
    };

    return (
        <div>
            <div className="font-link" style={headerTextStyle}>
                <span>
                    <FontAwesomeIcon icon={faUsers} style={iconStyle} />
                    {t('groupPanel.groups')}
                </span>
            </div>
            <div className="container">
                <div className="left-side">
                    <div style={{ padding: '60px' }}>
                        <CustomPaginationGroupTable updateTable={updateTableCallback} />
                    </div>
                </div>
                <div className="right-side">
                    <div style={{ flex: 1, padding: '20px' }}>
                        <div style={containerStyle} className="font-link">
                            <Box sx={boxStyle}>
                                <div className="font-link" style={miniHeaderTextStyle}>
                                    <span>
                                        {t('groupPanel.createGroup')}
                                    </span>
                                </div>
                                <List style={listStyle}>
                                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                        <ListItem>
                                            <div className="font-link">
                                                {t('groupPanel.groupName')}*
                                            </div>
                                        </ListItem>
                                        <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                            <TextField
                                                required
                                                id="outlined-required-email"
                                                style={{ backgroundColor: 'white', width: '100%' }}
                                                variant='standard'
                                                onChange={handleGroupNameChange}
                                                value={groupName}
                                            />
                                        </ListItem>

                                        <ListItem style={{ display: 'flex', justifyContent: 'right' }}>
                                            <ListItemButton onClick={handleClickOpenUserList}>
                                                <ListItemText>
                                                    <div className="font-link" style={buttonTextStyle}>
                                                        {t('groupPanel.editUserList')}
                                                    </div>
                                                </ListItemText>
                                            </ListItemButton>
                                        </ListItem>

                                        <ListItem>

                                            <TableContainer component={Paper} style={tableStyle}>
                                                <Table aria-label="simple table">
                                                    <TableHead>
                                                        <TableRow >
                                                            <TableCell>
                                                                <div className="font-link" style={tableHeaderStyle}>
                                                                    {t('groupPanel.userList')}*
                                                                </div>
                                                            </TableCell>
                                                        </TableRow>
                                                    </TableHead>
                                                    <TableBody>
                                                        <Paper
                                                            style={{
                                                                maxHeight: '100px',
                                                                width: '100%',
                                                                overflow: 'auto',
                                                                backgroundColor: 'rgba(131, 225, 180, 0.2)', // Adjust the alpha value as needed
                                                                boxShadow: 'none', // Remove the default box shadow
                                                            }}>
                                                            {selectedUsers.map((user) => (
                                                                <TableRow key={user.id} style={{width: '100%'}}>
                                                                    <TableCell style={{color: 'rgba(245, 249, 246, 1)', width: '100%'}}>
                                                                        {user.email}
                                                                    </TableCell>
                                                                    <TableCell align="right">
                                                                        <Button
                                                                        style = {{color: 'rgba(245, 249, 246, 1)'}}
                                                                            onClick={() => {
                                                                                setSelectedUsers((prevSelectedUsers) =>
                                                                                    prevSelectedUsers.filter((selectedUser) => selectedUser.id !== user.id)
                                                                                );
                                                                            }}
                                                                        >
                                                                            <FontAwesomeIcon icon={faTrash}/>
                                                                        </Button>
                                                                    </TableCell>
                                                                </TableRow>
                                                            ))}
                                                        </Paper>
                                                    </TableBody>
                                                </Table>
                                            </TableContainer>
                                        </ListItem>


                                        <ListItem style={{ display: 'flex', justifyContent: 'right' }}>
                                            <ListItemButton onClick={handleClickAddGroup}>
                                                <ListItemText>
                                                    <div className="font-link" style={buttonTextStyle}>
                                                        {t('groupPanel.add')}
                                                    </div>
                                                </ListItemText>
                                            </ListItemButton>
                                        </ListItem>
                                    </div>
                                </List>
                            </Box>
                        </div>
                    </div>
                </div>
            </div>


            <Dialog
                open={openUserList}
                onClose={handleCloseUserList}
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
                    {t('groupPanel.addUser')}
                </DialogTitle>
                <DialogContent>
                    <DialogContentText id="alert-dialog-description">
                        {t('groupPanel.addUserDescription')}
                    </DialogContentText>
                    <TextField
                        variant="outlined"
                        value={filter}
                        onChange={handleFilterChange}
                        style={{ width: '100%', paddingBottom: '2%', paddingTop: '8%' }}
                        InputProps={{
                            style: { backgroundColor: 'white' },
                        }}
                    />
                    <Paper
                        style={{
                            maxHeight: '300px',
                            overflow: 'auto',
                            backgroundColor: 'rgba(131, 225, 180, 0.2)', // Adjust the alpha value as needed
                            boxShadow: 'none', // Remove the default box shadow
                        }}>
                        <List dense sx={{ width: '100%', maxWidth: 360 }}>
                            {filteredRows.map((item) => {
                                const labelId = `checkbox-list-secondary-label-${item.id}`;
                                return (
                                    <ListItem
                                        key={item.id}
                                        secondaryAction={
                                            <Checkbox
                                                edge="end"
                                                onChange={handleToggle(item.id)}
                                                checked={checked.indexOf(item.id) !== -1}
                                                inputProps={{ 'aria-labelledby': labelId }}
                                            />
                                        }
                                        disablePadding
                                    >
                                        <ListItemButton>
                                            <ListItemText id={labelId} primary={item.email} />
                                        </ListItemButton>
                                    </ListItem>
                                );
                            })}
                        </List>
                    </Paper>
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleCloseUserList} style={dialoButtonTextStyle}>
                        {t('groupPanel.cancel')}
                    </Button>
                    <Button onClick={handleClickAddUsersGroup} autoFocus style={dialoButtonTextStyle}>
                        {t('groupPanel.add')}
                    </Button>
                </DialogActions>
            </Dialog>



            <Stack sx={{
                    width: '20%',
                    position: 'fixed',
                    bottom: 16 + alertHeight,
                    right: 16,
                    zIndex: 1000,
                }} spacing={2}>
                    {Object.values(validationErrors).map((error, index) => (
                        <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                            <AlertTitle>{t('groupPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>


                <Snackbar open={openAddGroupSuccessAlert} autoHideDuration={6000} onClose={handleAddGroupSuccessAlertClose}>
                    <Alert severity="success" sx={{ width: '100%' }} onClose={handleAddGroupSuccessAlertClose}>
                        {t('groupPanel.addGroupSuccess')}
                    </Alert>
                </Snackbar>
        </div >

    );
};

export default GroupPanel;