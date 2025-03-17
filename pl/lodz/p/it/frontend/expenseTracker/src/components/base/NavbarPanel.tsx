import React, { useState } from 'react';
import { AppBar, ButtonGroup, Box, Toolbar, IconButton, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useTranslation } from "react-i18next";
import "../../i18n";
import { useNavigate } from 'react-router-dom';
import { jwtDecode } from 'jwt-decode';
import { faRightFromBracket, faEarthEurope, faChartPie, faCalendarDays, faUsers, faChartBar, faUsersViewfinder } from '@fortawesome/free-solid-svg-icons';
import { useEffect } from 'react';
import { GUEST } from '../../consts';
import axios from 'axios';
import { API_URL } from '../../consts';
import { GroupResponseDto } from '../../types/GroupResponseDto';


const NavbarPanel = () => {
    const { t, i18n } = useTranslation();
    const [openDrawer, setOpenDrawer] = useState(false);
    const navigate = useNavigate();
    const logoWithText = require('../../assets/logoWithText.svg').default;
    const pl = require("../../assets/pl.svg").default;
    const gb = require("../../assets/gb.svg").default;
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const [roles, setRoles] = useState([GUEST]);
    const [currentRole, setCurrentRole] = useState(localStorage.getItem("role"));
    const [rows, setRows] = React.useState<GroupResponseDto[]>([]);

    const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    const appBarStyle = {
        backgroundColor: '#99EDC5',
    };

    const iconStyle = {
        color: '#99EDC5',
        fontSize: 100,
    };

    const listContainerStyle = {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center', // Align items horizontally, in this case to center
        justifyContent: 'center', // Center the content vertically
        height: '100%', // Make the container take full height
    };

    const toggleDrawer = () => {
        setOpenDrawer(!openDrawer);
    };

    const logOut = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        localStorage.removeItem("id");
        localStorage.clear();
        navigate('/');
    };

    const handleLanguageChange = (event: React.MouseEvent<HTMLElement>) => {
        i18n.changeLanguage(event.currentTarget.getAttribute("value")!);
        handleClose();
    }


    useEffect(() => {
        if(localStorage.getItem("token") !== null)
        {
            let config = {
                method: 'POST',
                maxBodyLength: Infinity,
                url: API_URL + '/auth/account/refresh-token',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('token')
                },
            };

            axios.request(config)
                .then((response) => {
                    localStorage.setItem("refreshToken", response.data.token);
                }).catch((error) => {
                    logOut();
                })
        }
    }, []);

    useEffect(() => {
        const storedToken = localStorage.getItem("token");

        if (storedToken)
        {
            try {
            const decodedToken = jwtDecode(storedToken);
            const decodedRoles = JSON.parse(JSON.stringify(decodedToken)).roles;
            localStorage.setItem("role", decodedRoles[0]);
            setRoles(roles); 
            setCurrentRole(localStorage.getItem("role"));
            
            }
            catch (e) {
                setRoles([GUEST]);
                setCurrentRole(GUEST);
            }
        }
    }, []);

    return (
        <div>
            <Box sx={{ flexGrow: 1 }}>
                {/*Panel nawigacyjny zawierający ikony powiadomień i menu. Menu jest w formie wysuwanego panelu z prawej strony.*/}
                <AppBar position="static" style={appBarStyle}>
                    <Toolbar>
                        <IconButton
                            color="inherit"
                            aria-label="menu"
                            onClick={() => { navigate('/'); }}
                            style={{ width: '10%', marginLeft: '5vh' }}
                        >
                            <img src={logoWithText} alt='logo' style={{ width: '200px', alignItems: 'left', alignContent: 'left', marginRight: 'auto' }} />

                        </IconButton>
                        <ButtonGroup variant="text" style={{ marginLeft: 'auto', alignItems: 'right', alignContent: 'right', display: 'flex', flexDirection: 'row' }} sx={{ mr: 2 }}>
                            <IconButton
                                color="inherit"
                                aria-label="menu"
                                onClick={handleClick}
                            >
                                <FontAwesomeIcon icon={faEarthEurope} size="xl" />
                            </IconButton>
                            <>
                            {localStorage.getItem("token") !== null && (
                            <IconButton
                                color="inherit"
                                aria-label="menu"
                                onClick={logOut}
                            >
                                <FontAwesomeIcon icon={faRightFromBracket} size="xl" />
                            </IconButton>
                         )}
                            </>

                            <>
                            {localStorage.getItem("token") !== null && (
                            <IconButton
                                color="inherit"
                                aria-label="menu"
                                onClick={toggleDrawer}
                            >
                                <MoreVertIcon sx={{ fontSize: 50 }} />
                            </IconButton>
                            )}
                            </>

                        </ButtonGroup>
                    </Toolbar>
                </AppBar>
            </Box>

            <>
                            {localStorage.getItem("token") !== null  && (
            <Drawer anchor="right" open={openDrawer} onClose={toggleDrawer} PaperProps={{ style: { width: '25%' } }}>
                <div>
                {localStorage.getItem("role") === "USER" && (
                    <List sx={listContainerStyle}>
                        <ListItemButton>
                            <ListItemIcon>
                                <FontAwesomeIcon icon={faUsers} style={iconStyle} onClick={() => { navigate('/group'); toggleDrawer() }} />
                            </ListItemIcon>
                        </ListItemButton>
                        <ListItemText primary={t('navbarPanel.groups')} />

                            <>
                        <ListItemButton>
                            <ListItemIcon>
                                <FontAwesomeIcon icon={faCalendarDays} style={iconStyle} onClick={() => { navigate('/calendar'); toggleDrawer() }} />
                            </ListItemIcon>
                        </ListItemButton>
                        <ListItemText primary={t('navbarPanel.calerndar')} />

                        <ListItemButton>
                            <ListItemIcon>
                                <FontAwesomeIcon icon={faChartPie} style={iconStyle} onClick={() => { navigate('/category'); toggleDrawer() }} />
                            </ListItemIcon>
                        </ListItemButton>
                        <ListItemText primary={t('navbarPanel.categories')} />

                        <ListItemButton>
                            <ListItemIcon>
                                <FontAwesomeIcon icon={faChartBar} style={iconStyle} onClick={() => { navigate('/analysis'); toggleDrawer() }} />
                            </ListItemIcon>
                        </ListItemButton>
                        <ListItemText primary={t('navbarPanel.analysis')} />
                        </>

                    </List>
                                            )}
                    <Box>
                        <List>
                            <ListItemButton>
                                <ListItemIcon>
                                    <AccountCircleIcon style={iconStyle} onClick={() => { navigate('/my-account'); toggleDrawer() }} />
                                </ListItemIcon>
                                <ListItemText primary={t('navbarPanel.account')} />
                            </ListItemButton>
                        </List>
                    </Box>

                    {localStorage.getItem("role") === "ADMIN" && (
                    <Box>
                        <List>
                            <ListItemButton style={{paddingBottom: '30px'}} >
                                <ListItemIcon>
                                    <FontAwesomeIcon icon={faUsers} style={iconStyle} onClick={() => { navigate('/accounts'); toggleDrawer() }}  />
                                </ListItemIcon>
                                <ListItemText style ={{ paddingLeft: "10pt"}} primary={t('navbarPanel.accountsList')} />
                            </ListItemButton>
                            <ListItemButton>
                                <ListItemIcon>
                                    <FontAwesomeIcon icon={faUsersViewfinder} style={iconStyle} onClick={() => { navigate('/groups'); toggleDrawer() }}  />
                                </ListItemIcon>
                                <ListItemText style ={{ paddingLeft: "10pt"}} primary={t('navbarPanel.groupsList')} />
                            </ListItemButton>
                        </List>
                    </Box>
                    )}
                </div>
            </Drawer>
                            )}
            </>

            <Menu
                id="basic-menu"
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
            >
                <MenuItem value={"plPL"} onClick={handleLanguageChange}>
                    <img src={pl} alt="plPL" style={{ width: '30%' }}/>
                </MenuItem>
                <MenuItem value={"enUS"} onClick={handleLanguageChange}>
                    <img src={gb} alt="enUS" style={{ width: '30%' }}/>
                </MenuItem>
            </Menu>
        </div>
    );
}

export default NavbarPanel;
