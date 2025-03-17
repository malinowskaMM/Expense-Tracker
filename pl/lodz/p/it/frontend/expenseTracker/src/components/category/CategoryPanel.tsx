import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faChartPie } from '@fortawesome/free-solid-svg-icons';
import { Box, ListItemButton, ListItemText, Select, List, Stack, AlertTitle, ListItem, TextField, MenuItem, Snackbar, Alert } from '@mui/material';
import { ChromePicker, ColorResult } from 'react-color';
import React, { useEffect, useState } from 'react';
import reactCSS from 'reactcss'
import CustomPaginationCategoryTable from './CustomPaginationCategoryTable';
import axios from 'axios';
import { API_URL } from '../../consts';
import { GroupResponseDto } from '../../types/GroupResponseDto';
import { CategoryCreateRequestDto } from '../../types/CategoryCreateRequestDto';
import { set, useForm } from 'react-hook-form';

const CategoryPanel = () => {
    const { t, i18n } = useTranslation();
    const [displayColorPicker, setDisplayColorPicker] = useState(false);
    const [colorState, setColorState] = useState("#6AE09E");
    const [group, setGroup] = useState('');
    const [groups, setGroups] = React.useState<GroupResponseDto[]>([]);
    const { register, handleSubmit, reset } = useForm<CategoryCreateRequestDto>();
    const [openCategoryAddFailedAlert, setOpenCategoryAddFailedAlert] = React.useState(false);
    const [reloadKey, setReloadKey] = useState(0);
    const [validationErrors, setValidationErrors] = useState<string[]>([]);
    const [alertHeight, setAlertHeight] = useState(0);

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '70vh',
        width: '50vw',
    };

    const boxStyle = {
        marginTop: '35vh',
        backgroundColor: 'rgba(153, 237, 197, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '700px',
        width: '80%',
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


    const handleColorPicker = () => {
        setDisplayColorPicker(!displayColorPicker);
    };

    const handleClose = () => {
        setDisplayColorPicker(false);
    };

    const handleChange = (color: ColorResult) => {
        setColorState(color.hex);
        register("color");
        validateCategoryColor(color.hex);
    };

    const handleGroupChange = (event: React.ChangeEvent<{ value: unknown }>) => {
        setGroup(event.target.value as string);
        validateCategoryGroup(event.target.value as string);
    };

    const handleCategoryAddFailedAlertClose = (event?: React.SyntheticEvent, reason?: string) => {
        if (reason === 'clickaway') {
            return;
        }
        setOpenCategoryAddFailedAlert(false);
    }

    const handleCategoryAddChange = (event: React.SyntheticEvent<unknown>, reason?: string) => {
        onSubmit();
    };

    const onSubmit = handleSubmit((data: CategoryCreateRequestDto) => {
        data.color = colorState;
        data.accountId = localStorage.getItem("id") !== undefined ? localStorage.getItem("id") : " ";

        if (data.accountId == null || data.name === '' || data.name == null || data.description === '' || data.description == null || data.color == null || data.groupId == null) {
            setOpenCategoryAddFailedAlert(true);
            return;
        }

        let config = {
            method: 'POST',
            url: API_URL + '/categories/category',
            headers: {
                'Content-Type': 'application/json',
                 'Authorization': 'Bearer ' + localStorage.getItem('token')
            },
            data: JSON.stringify(data),
        };

        const featchData = async () => {
            axios.request(config)
                .then((response) => {
                    setColorState("#6AE09E");

                    reset();
                    featchGroupData();
                    setReloadKey((prevKey) => prevKey + 1);
                }).catch((error) => {
                    setOpenCategoryAddFailedAlert(true);
                })

        }

        featchData();

    });

    useEffect(() => {
        featchGroupData();
    }   , []);

    let config = {
        method: 'GET',
        url: API_URL + '/groups/account/' + localStorage.getItem("id"),
        headers: {
            'Content-Type': 'application/json',
             'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
    };

    const featchGroupData = async () => {
        axios.request(config)
            .then((response) => {
                setGroups(response.data.groups);
            }).catch((error) => {
            })
        }

    const handleCategoryNameChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        validateCategoryName(event.target.value);
    }
    
    const handleCategoryDescriptionChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        validateCategoryDescription(event.target.value);
    }

    const handleAlertLoad = (event: React.ChangeEvent<HTMLInputElement>) => {
        setAlertHeight(event.target.clientHeight);
    };

    const validateCategoryName = (name: string) => {
        const isValid = name.length > 2;
        updateValidationErrors('name', isValid);
        return isValid;
    }

    const validateCategoryDescription = (description: string) => {
        const isValid = description.length > 2;
        updateValidationErrors('description', isValid);
        return isValid;
    }

    const validateCategoryColor = (color: string) => {
        const isValid = color !== '';
        updateValidationErrors('color', isValid);
        return isValid;
    }

    const validateCategoryGroup = (group: string) => {
        const isValid = group !== '';
        updateValidationErrors('group', isValid);
        return isValid;
    }
    
    const updateValidationErrors = (field: string, isValid: boolean) => {
        setValidationErrors((prevErrors) => {
            const updatedErrors = { ...prevErrors };
            if (!isValid) {
                switch (field) {
                    case 'name':
                        updatedErrors[field] = t('categoryPanel.categoryNameValidation');
                        break;
                    case 'description':
                        updatedErrors[field] = t('categoryPanel.categoryDescriptionValidation');
                        break;
                    case 'color':
                        updatedErrors[field] = t('categoryPanel.categoryColorValidation');
                        break;
                    case 'group':
                        updatedErrors[field] = t('categoryPanel.categoryGroupValidation');
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

    return (
        <div>
            <div className="font-link" style={headerTextStyle}>
                <div>
                    <FontAwesomeIcon icon={faChartPie} style={iconStyle} />
                    {t('categoryPanel.categories')}
                </div>
            </div>
            <div className="container">
                <div className="left-side">
                    <div className="font-link-header" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', paddingTop: '10vh' }}>
                        {t('categoryPanel.yourCategories')}
                    </div>
                    <div style={{ padding: '60px' }}>
                        <CustomPaginationCategoryTable key={reloadKey} />
                    </div>
                </div>
                <div className="right-side">
                    <div style={{ flex: 1, padding: '20px' }}>
                        <div style={containerStyle} className="font-link">
                            <Box sx={boxStyle}>
                                <div className="font-link" style={miniHeaderTextStyle}>
                                    <span>
                                        {t('categoryPanel.createCategory')}
                                    </span>
                                </div>
                                <List style={listStyle}>
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
                                                    required
                                                    id="outlined-required-category-name"
                                                    style={{ backgroundColor: 'white', width: '100%' }}
                                                    onChange={handleCategoryNameChange}
                                                />
                                            </ListItem>

                                            <ListItem>
                                                <div className="font-link">
                                                    {t('categoryPanel.categoryColor')}*
                                                </div>
                                            </ListItem>
                                            <ListItem>
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
                                                    required
                                                    id="outlined-required-category-description"
                                                    style={{ backgroundColor: 'white', width: '100%' }}
                                                    onChange={handleCategoryDescriptionChange}
                                                />
                                            </ListItem>

                                            <ListItem>
                                                <div className="font-link">
                                                    {t('categoryPanel.group')}*
                                                </div>
                                            </ListItem>
                                            <ListItem style={{ display: 'flex', justifyContent: 'left' }}>
                                                <Select
                                                    {...register("groupId")}
                                                    labelId="demo-simple-select-label"
                                                    id="group-select"
                                                    value={group}
                                                    label="Group"
                                                    onChange={handleGroupChange}
                                                    style={{ width: '50%' }}
                                                >
                                                    {groups.map((groupItem) => (
                                                        <MenuItem key={groupItem.groupId} value={groupItem.groupId}>
                                                            {groupItem.groupName}
                                                        </MenuItem>
                                                    ))}
                                                </Select>
                                            </ListItem>

                                            <ListItem style={{ display: 'flex', justifyContent: 'right' }}>
                                                <ListItemButton onClick={handleCategoryAddChange}>
                                                    <ListItemText>
                                                        <div className="font-link" style={buttonTextStyle}>
                                                            {t('categoryPanel.addCategory')}
                                                        </div>
                                                    </ListItemText>
                                                </ListItemButton>
                                            </ListItem>
                                        </div>
                                    </Box>
                                </List>
                            </Box>
                        </div>
                    </div>
                </div>
            </div>
            <Snackbar open={openCategoryAddFailedAlert} autoHideDuration={6000} onClose={handleCategoryAddFailedAlertClose}>
                <Alert severity="error" sx={{ width: '100%' }} onClose={handleCategoryAddFailedAlertClose}>
                    {t('categoryPanel.addCategoryFailed')}
                </Alert>
            </Snackbar>


            <Stack sx={{
                    width: '20%',
                    position: 'fixed',
                    bottom: 16 + alertHeight,
                    right: 16,
                    zIndex: 1000,
                }} spacing={2}>
                    {Object.values(validationErrors).map((error, index) => (
                        <Alert key={index} severity="error" onLoad={handleAlertLoad}>
                            <AlertTitle>{t('categoryPanel.formValidation')}</AlertTitle>
                            {error}
                        </Alert>
                    ))}
                </Stack>
        </div >

    );
};

export default CategoryPanel;