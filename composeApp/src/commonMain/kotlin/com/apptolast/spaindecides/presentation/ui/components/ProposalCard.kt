package com.apptolast.spaindecides.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import org.jetbrains.compose.resources.stringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.proposal_vote_down
import spaindecides.composeapp.generated.resources.proposal_vote_up

/**
 * Card component for displaying a proposal with voting buttons.
 *
 * @param proposal The proposal to display (with user vote information)
 * @param onUpvote Callback when upvote is clicked
 * @param onDownvote Callback when downvote is clicked
 * @param modifier Optional modifier
 */
@Composable
fun ProposalCard(
    proposal: ProposalWithUserVote,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vote buttons column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(48.dp)
            ) {
                // Upvote button
                IconButton(
                    onClick = onUpvote,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (proposal.userVote == 1) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(Res.string.proposal_vote_up),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Vote count
                Text(
                    text = proposal.formattedVotes,
                    style = MaterialTheme.typography.labelLarge,
                    color = when {
                        proposal.userVote == 1 -> MaterialTheme.colorScheme.primary
                        proposal.userVote == -1 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                // Downvote button
                IconButton(
                    onClick = onDownvote,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (proposal.userVote == -1) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.proposal_vote_down),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Proposal text
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
