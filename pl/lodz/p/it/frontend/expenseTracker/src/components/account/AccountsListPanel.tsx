import { useTranslation } from "react-i18next";
import './CustomPaginationActionsTable';
import CustomPaginationActionsTable from "./CustomPaginationActionsTable";
import * as React from 'react';
import { TransitionProps } from '@mui/material/transitions';
import AddIcon from '@mui/icons-material/Add';
import { Fab, Button, Dialog, DialogActions, DialogTitle, Slide } from '@mui/material';
import AccountTabPanel from './AccountTabPanel'

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
      children: React.ReactElement<any, any>;
    },
    ref: React.Ref<unknown>,
  ) {
    return <Slide direction="up" ref={ref} {...props} />;
  });

export default function AccountsListPanel() {
    const { t, i18n } = useTranslation();
    const [openAddUser, setOpenAddUser] = React.useState(false);

    const buttonTextStyle = {
        textAlign: 'center' as const,
        backgroundColor: 'rgba(131, 225, 180, 1)',
        borderRadius: '5vh',
        color: '#1B2024',
        padding: '1vh',
    };

    const handleClickOpenAddUser = () => {
        setOpenAddUser(true);
    };

    const handleCloseAddUser = () => {
        setOpenAddUser(false);
    };

    const tableCentralizationStyle : React.CSSProperties = {
        textAlign: 'center' as const,
        fontSize: '4.0rem',
        color: 'rgba(43, 42, 42, 0.7)',
        paddingTop: '10vh',
        alignContent: 'center',
        justifyContent: 'center',
        width: '80%',
    };

    const boxStyle: React.CSSProperties = {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
    };

    return (
        <div>
            <div className="right-side-60">
                <Fab color="primary" aria-label="add" onClick={handleClickOpenAddUser}>
                    <AddIcon />
                </Fab>
            </div>
            <div style={boxStyle}>
            <div style={tableCentralizationStyle}>
                <CustomPaginationActionsTable />
            </div>
            </div>

            <Dialog
                open={openAddUser}
                TransitionComponent={Transition}
                keepMounted
                onClose={handleCloseAddUser}
                aria-describedby="alert-dialog-slide-description"
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
                <DialogTitle>{t('accountsListPanel.addAccount')}</DialogTitle>
                <AccountTabPanel/>
                <DialogActions>
                    <Button onClick={handleCloseAddUser} style={buttonTextStyle}>
                        <div className="font-link">
                            {t('accountsListPanel.exit')}
                        </div>
                        </Button>
                </DialogActions>
            </Dialog>


        </div>
    )

}