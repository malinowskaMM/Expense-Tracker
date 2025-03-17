
import React from 'react';
import { TransitionProps } from '@mui/material/transitions';
import { Slide, Fab } from '@mui/material';
import CustomPaginationAdminGroupTable from './CustomPaginationAdminGroupTable';

const Transition = React.forwardRef(function Transition(
    props: TransitionProps & {
      children: React.ReactElement<any, any>;
    },
    ref: React.Ref<unknown>,
  ) {
    return <Slide direction="up" ref={ref} {...props} />;
  });

export default function GroupsListPanel() {

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
            <div style={boxStyle}>
            <div style={tableCentralizationStyle}>
            <CustomPaginationAdminGroupTable />
            </div>
            </div>
        </div>
    )

}