import { useTranslation } from "react-i18next";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCircleUser } from '@fortawesome/free-solid-svg-icons';
import { ListItemButton, ListItemText, Box, List, ListItem, TextField } from '@mui/material';
import "../../i18n";

const AccountDetailsPanel = () => {
    const { t, i18n } = useTranslation();

    const containerStyle = {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100%',
        width: '100%',
    };

    const boxStyle = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        width: '50%',
        paddingTop: '5vh',
    };

    const headerTextStyle = {
        textAlign: 'center' as const,
        fontSize: '4.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
        paddingTop: '5vh',
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

    const buttonRedTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(255, 48, 48, 1)',
        borderRadius: '5vh',
        fontSize: '1.5rem',
        color: 'rgba(245, 249, 246, 1)',
    };

    return (
        <div>
            <div className="font-link" style={headerTextStyle}>
                <span>
                    <FontAwesomeIcon icon={faCircleUser} style={iconStyle} />
                    {t('accountPanel.account')}
                </span>
            </div>

            <div className="container">
                <div className="left-side">
                    <div style={containerStyle}>
                        <Box sx={boxStyle}>
                            <List style={listStyle}>
                                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                                    <ListItem className="font-link-header">
                                        <div>
                                            {t('accountPanel.changeEmail')}
                                        </div>
                                    </ListItem>
                                    <ListItem className="font-link">
                                        <div>
                                            {t('accountPanel.addressEmail')}
                                        </div>
                                    </ListItem>
                                    <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                        <TextField
                                            disabled
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemButton>
                                            <ListItemText>
                                                <div className="font-link" style={buttonTextStyle}>
                                                    {t('accountPanel.changeEmailButton')}
                                                </div>
                                            </ListItemText>
                                        </ListItemButton>
                                    </ListItem>

                                    <ListItem className="font-link-header">
                                        <div>
                                            {t('accountPanel.changePassword')}
                                        </div>
                                    </ListItem>
                                    <ListItem className="font-link">
                                        <div>
                                            {t('accountPanel.password')}
                                        </div>
                                    </ListItem>
                                    <ListItem style={{ display: 'flex', justifyContent: 'center' }}>
                                        <TextField
                                            disabled
                                            id="outlined-required-email"
                                            style={{ backgroundColor: 'white', width: '100%' }}
                                            variant='standard'
                                        />
                                    </ListItem>
                                    <ListItem>
                                        <ListItemButton>
                                            <ListItemText>
                                                <div className="font-link" style={buttonTextStyle}>
                                                    {t('accountPanel.changePasswordButton')}
                                                </div>
                                            </ListItemText>
                                        </ListItemButton>
                                    </ListItem>

                                    <ListItem className="font-link-header">
                                        <div>
                                            {t('accountPanel.deleteAccount')}
                                        </div>
                                    </ListItem>
                                    <ListItem>
                                        <ListItemButton>
                                            <ListItemText>
                                                <div className="font-link" style={buttonRedTextStyle}>
                                                    {t('accountPanel.deleteAccountButton')}
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
            <div className="right-side">
            </div>
        </div>
    )
}

export default AccountDetailsPanel;